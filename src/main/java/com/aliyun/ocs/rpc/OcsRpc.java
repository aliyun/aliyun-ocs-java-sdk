package com.aliyun.ocs.rpc;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.aliyun.ocs.OcsAccount;
import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.command.binary.Command;
import com.aliyun.ocs.command.binary.single.MemcachedAuthCommand;
import com.aliyun.ocs.command.binary.single.MemcachedNoopCommand;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.MemcachedOpCode;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;
import com.aliyun.ocs.rpc.future.OcsFutureBatchReply;
import com.aliyun.ocs.rpc.future.OcsFutureInternal;
import com.aliyun.ocs.rpc.future.OcsFutureReply;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;
import com.aliyun.ocs.util.DeamondThreadFactory;
import com.aliyun.ocs.util.Util;

public class OcsRpc {
	Log logger = LogFactory.getLog(OcsRpc.class);
	private OcsAccount account = null;
	// private OcsConnection conn = null;
	private String domain;
	private ClientBootstrap bootstrap = null;
	private static final long DEFAULT_CONNECT_TIMEOUT = 500L;
	private static final int INIT_HASH_SIZE_FACTOR = 2;
	private static final int DEFAULT_CONNECTION_COUNT = 1;
	private OcsRpcContext rpcContext = null;
	private OcsTimeoutBackgrand backgrand = new OcsTimeoutBackgrand();
	private static int workerThreadCount = Runtime.getRuntime().availableProcessors() / 4 + 1;
	private static int bossThreadCount = (Runtime.getRuntime().availableProcessors() + 7) / 8;
	private static int maxConnectionCount = DEFAULT_CONNECTION_COUNT;
	private static String workerThreadCountKey = "ocs.nio.workerThreadCount";
	private static String bossThreadCountKey = "ocs.nio.bossThreadCount";
	private static String maxConnectionCountKey = "ocs.nio.maxConnectionCount";
	private static ExecutorService bossThreadPool = null;
	private static ExecutorService workerThreadPool = null;
	private static ChannelFactory nioChannelFactory = null;
	private OcsMessageFactory messageFactory = null;
	private OcsOptions defaultOptions = OcsOptions.defaultOptions();
	private Random r = new Random();

	private Map<Integer, OcsConnection> connections = new ConcurrentHashMap<Integer, OcsConnection>();
	private Map<Integer, ReentrantReadWriteLock> conectionsLock = new ConcurrentHashMap<Integer, ReentrantReadWriteLock>();
	static {
		workerThreadCount = Util.getSystemConfigrationInt(workerThreadCountKey, 1);
		bossThreadCount = Util.getSystemConfigrationInt(bossThreadCountKey, 1);
		maxConnectionCount = Util.getSystemConfigrationInt(maxConnectionCountKey, DEFAULT_CONNECTION_COUNT);
		bossThreadPool = Executors.newCachedThreadPool(new DeamondThreadFactory("ocs-client-core-boss"));
		workerThreadPool = Executors.newCachedThreadPool(new DeamondThreadFactory("ocs-client-core-worker"));
		nioChannelFactory = new NioClientSocketChannelFactory(bossThreadPool, workerThreadPool, bossThreadCount, workerThreadCount);
	}

	public OcsRpc(String domain, OcsAccount account) {
		
		backgrand.start();

		this.domain = domain;
		this.setAccount(account);
		for (int i = 0; i < maxConnectionCount; ++i) {
			connections.put(i, new OcsConnection(domain, account, i));
			conectionsLock.put(i, new ReentrantReadWriteLock());
		}
		this.rpcContext = new OcsRpcContext(backgrand);
		bootstrap = new ClientBootstrap(nioChannelFactory);
		setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new OcsFrameDecoder(), new OcsMessageHandler(rpcContext));
			}
		});
		messageFactory = OcsMessageFactory.getOcsMessageFactoryInstance();
		logger.info("ocs client start successful, domain: " + this.domain + ", user name: " + this.account.getUsername() + ", rpc boosthreads: "
				+ bossThreadCount + ", workerthreads: " + workerThreadCount + ", maxConnectionCount: " + maxConnectionCount);
	}

	public OcsFutureReply asyncCall(final Command command, final OcsTranscoder trans, final String key, int timeout) throws OcsException {
		OcsConnection conn = selectConnection();
		if (!conn.isAuthenticated() && account.isExemptPassword() == false) {
			sparkAuthentication(conn);
		}
		OcsFutureInternal ocsFuture = asyncCall(conn, command, null, timeout);
		return new OcsFutureReply(ocsFuture, trans, key);
	}

	private OcsFutureInternal asyncCall(OcsConnection conn, Command command, Collection<Integer> c, int timeout) throws OcsException {
		int opaque = conn.generateOpaque();
		final BinaryMemcachedMessage m = messageFactory.buildFromRequest(command);
		return sendMessage(conn, m, command.getLazyDecoder(), opaque, c, timeout);
	}

	private OcsFutureInternal sendMessage(final OcsConnection conn, final BinaryMemcachedMessage message, final OcsLazyDecoder lazyDecoder, int opaque,
			Collection<Integer> c, int timeout) throws OcsException {
		final OcsChannel channel = conn.getChannel();
		backgrand.registRpcID(channel, opaque, timeout);
		message.setOpaque(opaque);
		if (logger.isDebugEnabled()) {
			logger.debug("Send request: { Opcode: " + message.getOpcode() + ", Opaque: " + message.getOpaque() + ", " + channel.toString());
		}
		final OcsFutureInternal ocsFuture = channel.registCallTask(opaque);
		ocsFuture.setLazyDecoder(lazyDecoder);
		if (c != null)
			ocsFuture.setOpaques(c);
		ChannelFutureListener listener = new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future != null) {
					channel.decAndGetWaitConnectCount();
					if (future.getCause() != null) {
						ocsFuture.setException(future.getCause());
						backgrand.removeRpcID(channel.getRpcID());
						channel.getAndRemoveCallTask(message.getOpaque());
						return;
					}
					channel.waitConnect(0);
				}
				channel.sendPacket(message, ocsFuture);
			}
		};

		if (channel.isReady() == false) {
			ChannelFuture connectFuture = channel.getConnectFuture();
			if (connectFuture == null) {
				logger.debug("connection was not ready");
				throw new OcsException("connection was not ready");
			}
			if (channel.getWaitConnectCount() > 128) {
				ocsFuture.setConnectFuture(connectFuture);
			} else {
				channel.incAndGetWaitConnectCount();
				connectFuture.addListener(listener);
			}
		} else {
			try {
				listener.operationComplete(null);
			} catch (Exception e) {
				logger.error("listenner exception, remote ip: " + channel.getDestAddress(), e);
				throw new OcsException("listerner exception: " + e.getMessage());
			}
		}
		return ocsFuture;
	}

	public OcsFutureBatchReply asyncCall(final Command command, final OcsTranscoder trans, boolean write, int dummyStatus, int timeout)
			throws OcsException {
		OcsConnection conn = selectConnection();
		if (!conn.isAuthenticated() && account.isExemptPassword() == false) {
			sparkAuthentication(conn);
		}
		Map<String, Integer> opaques = asyncCallBatchImpl(conn, command);
		MemcachedNoopCommand noop = new MemcachedNoopCommand(this, MemcachedOpCode.NOOP);
		OcsFutureInternal noopFuture = asyncCall(conn, noop, opaques.values(), timeout);
		noopFuture.setAccessWrite(write);

		return new OcsFutureBatchReply(conn.getChannel(), opaques, noopFuture, noop.getKey(), trans, command.getLazyDecoder(), dummyStatus);
	}

	private Map<String, Integer> asyncCallBatchImpl(OcsConnection conn, Command command) throws OcsException {
		final Map<String, BinaryMemcachedMessage> rpcMessages = messageFactory.buildFromBatchRequest(command);
		Map<String, Integer> futures = new HashMap<String, Integer>(rpcMessages.size() * INIT_HASH_SIZE_FACTOR);
		for (Map.Entry<String, BinaryMemcachedMessage> entry : rpcMessages.entrySet()) {
			String key = entry.getKey();
			BinaryMemcachedMessage m = entry.getValue();
			int opaque = conn.generateOpaque();
			sendMessage(conn, m, command.getLazyDecoder(), opaque);
			futures.put(key, opaque);
		}
		return futures;
	}

	private void sendMessage(final OcsConnection conn, final BinaryMemcachedMessage message, final OcsLazyDecoder lazyDecoder, int opaque)
			throws OcsException {
		message.setOpaque(opaque);
		final OcsChannel channel = conn.getChannel();
		if (logger.isDebugEnabled()) {
			logger.debug("Send request: { Opcode: " + message.getOpcode() + ", Opaque: " + message.getOpaque() + ", " + channel.toString());
		}
		ChannelFutureListener listener = new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future != null) {
					channel.decAndGetWaitConnectCount();
					channel.waitConnect(0);
				}
				channel.sendPacket(message, null);
			}
		};

		if (channel.isReady() == false) {
			ChannelFuture connectFuture = channel.getConnectFuture();
			if (connectFuture == null) {
				logger.debug("connection was not ready");
				throw new OcsException("connection was not ready");
			}
			if (channel.getWaitConnectCount() > 128) {
				//ocsFuture.setConnectFuture(connectFuture);
			} else {
				channel.incAndGetWaitConnectCount();
				connectFuture.addListener(listener);
			}
		} else {
			try {
				listener.operationComplete(null);
			} catch (Exception e) {
				logger.error("listenner exception, remote ip: " + channel.getDestAddress(), e);
				throw new OcsException("listerner exception: " + e.getMessage());
			}
		}
	}

	private void sparkAuthentication(OcsConnection conn) throws OcsException {
		if (conn.needAuthStartNow()) {
			byte[] memchanism = conn.getAuthMechanism();
			byte[] eveluate = conn.getAuthEveluate();
			MemcachedAuthCommand command = new MemcachedAuthCommand(this, memchanism, eveluate);
			OcsFutureInternal ocsFuture = asyncCall(conn, command, null, defaultOptions.getTimeout());
			OcsFuture<OcsResult> of = new OcsFutureReply(ocsFuture, defaultOptions.getTranscoder(), "auth");
			OcsResult result = null;
			try {
				result = of.get();
				if (result.getStatus() == OcsReplyStatus.REPLY_SUCCESS) {
					logger.info("auth successful, domain: " + this.domain + ", user name: " + this.account.getUsername() + ", result status: "
							+ result.getStatus());
					conn.authCompelete(true);
				}
			} catch (InterruptedException e) {
				logger.info("auth fail, domain: " + this.domain + ", user name: " + this.account.getUsername() + ", e: " + e);
				conn.authCompelete(false);
			} catch (ExecutionException e) {
				logger.info("auth fail, domain: " + this.domain + ", user name: " + this.account.getUsername() + ", e: " + e);
				conn.authCompelete(false);
			}
		}
	}

	public OcsAccount getAccount() {
		return account;
	}

	public void setAccount(OcsAccount account) {
		this.account = account;
	}

	public void setConnectTimeout(long timeout) {
		bootstrap.setOption("connectTimeoutMillis", timeout);
	}

	public ChannelFuture createSession(SocketAddress addr, ChannelFutureListener listener) {
		ChannelFuture future = bootstrap.connect(addr);
		future.addListener(listener);
		return future;
	}

	public void deleteSession(OcsConnection conn) {
		OcsChannel deleteChannel = null;
		ReentrantReadWriteLock lock = conectionsLock.get(conn.getKey());
		try {
			lock.writeLock().lock();
			deleteChannel = conn.getChannel();
			conn.removeChannel();
		} finally {
			lock.writeLock().unlock();
		}
		if (deleteChannel != null) {
			deleteChannel.close();
		}
	}

	private OcsConnection selectConnection() throws OcsException {
		OcsConnection conn = null;
		if (maxConnectionCount == 1) {
			conn = connections.get(0);
		} else {
			conn = connections.get(r.nextInt(maxConnectionCount) % maxConnectionCount);
		}
		OcsChannel existChannel = null;
		ReentrantReadWriteLock lock = conectionsLock.get(conn.getKey());
		try {
			lock.readLock().lock();
			existChannel = conn.getChannel();
			if (existChannel == null) {
				// NOTE: lookup new IP from DNS.
				SocketAddress addr = Util.cast2SocketAddress(this.domain);
				logger.info("new connection starts, demain: " + this.domain + ", remote ip: " + addr);
				OcsChannel channel = new OcsChannel(addr, messageFactory, this, conn);
				conn.setChannel(channel);
				if (existChannel == null) {
					existChannel = channel;
					ChannelFuture connectFuture = existChannel.connect();
					channel.setConnectFuture(connectFuture);
				}
			}
		} finally {
			lock.readLock().unlock();
		}

		if (existChannel.getCause() != null) {
			logger.error("connection occours exception, delete channel, demain: " + this.domain + ", remote ip: " + existChannel.getDestAddress(),
					existChannel.getCause());
			deleteSession(conn);
			throw new OcsException(existChannel.getCause());
		}

		if (existChannel.isReady()) {
			return conn;
		} else {
			if (existChannel.waitConnect(0)) {
				if (existChannel.getCause() != null) {
					logger.error("connection occours exception, delete channel, demain: " + this.domain + ", remote ip: " + existChannel.getDestAddress(),
							existChannel.getCause());
					deleteSession(conn);
					throw new OcsException(existChannel.getCause());
				} else if (existChannel.isReady()) {
					return conn;
				} else {
					// Never reach here
				}
			}
			logger.error("wait to connect exception, delete channel, demain: " + this.domain + ", remote ip: " + existChannel.getDestAddress(),
					existChannel.getCause());
			deleteSession(conn);
			throw new OcsException("wait to connect exception");
		}
	}

	public void reconnect() {
	}
}
