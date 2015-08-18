package com.aliyun.ocs.rpc;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.rpc.future.OcsFutureInternal;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;

public class OcsChannel {
	private Log logger = LogFactory.getLog(OcsChannel.class);
	private OcsRpc rpcInstance;
	private OcsConnection conn;
	private Channel channelImpl = null;
	private Throwable cause = null;
	private Object sessionLock = new Object();
	private Boolean finished = false;
	private SocketAddress destAddress;
	private OcsMessageFactory factory;
	private ChannelFuture connectFuture;
	private AtomicInteger waitConnectCount = new AtomicInteger(0);
	private static final int DEFAULT_TASK_SIZE = 1000;
	private OcsRpcID rpcID = null;
	private ConcurrentHashMap<Integer, OcsFutureInternal> tasks = new ConcurrentHashMap<Integer, OcsFutureInternal>(DEFAULT_TASK_SIZE, 2);
	private ConcurrentHashMap<Integer, Integer> batchWriteResultHeap = new ConcurrentHashMap<Integer, Integer>();//(DEFAULT_TASK_SIZE, 2);
	private ConcurrentHashMap<Integer, OcsReplyMessageWrapper> batchReadResultHeap = new ConcurrentHashMap<Integer, OcsReplyMessageWrapper>(DEFAULT_TASK_SIZE,
			2);

	private OcsReplyMessageWrapper cachedRpcMessage;

	public void setConnectFuture(ChannelFuture connectFuture) {
		this.connectFuture = connectFuture;
	}

	public OcsReplyMessageWrapper getCachedRpcMessage() {
		return cachedRpcMessage;
	}

	public void setCachedRpcMessage(OcsReplyMessageWrapper cachedRpcMessage) {
		this.cachedRpcMessage = cachedRpcMessage;
	}

	public int incAndGetWaitConnectCount() {
		return waitConnectCount.getAndIncrement();
	}

	public int decAndGetWaitConnectCount() {
		return waitConnectCount.decrementAndGet();
	}

	public int getWaitConnectCount() {
		return waitConnectCount.get();
	}

	public static OcsChannel getOcsChannel(Channel ctx) {
		return (OcsChannel) ctx.getAttachment();
	}

	public OcsMessageFactory getPacketFactory() {
		return factory;
	}

	ChannelFutureListener ioFutureListener = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			cause = future.getCause();
			channelImpl = future.getChannel();
			channelImpl.setAttachment(OcsChannel.this);

			synchronized (sessionLock) {
				finished = true;
				sessionLock.notifyAll();
			}
		}
	};

	public OcsChannel(SocketAddress destAddress, OcsMessageFactory factory, OcsRpc ocsRpc, OcsConnection conn) {
		this.rpcInstance = ocsRpc;
		this.destAddress = destAddress;
		this.factory = factory;
		this.conn = conn;
	}

	public ChannelFuture connect() {
		return rpcInstance.createSession(this.destAddress, ioFutureListener);
	}

	public ChannelFuture getConnectFuture() {
		return connectFuture;
	}

	public SocketAddress getDestAddress() {
		return destAddress;
	}

	public boolean isReady() {
		return channelImpl != null && cause == null;
	}

	public Throwable getCause() {
		return cause;
	}

	public ChannelFuture sendPacket(final BinaryMemcachedMessage message, final OcsFutureInternal rpcFuture) {
		ChannelBuffer out = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, message.size());
		message.encodeTo(out);
		out.resetReaderIndex();
		ChannelFuture future = channelImpl.write(out);
		if (rpcFuture != null) {

			future.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.getCause() == null) {

					} else {
						logger.error("send packet error, remote ip: " + future.getChannel().getRemoteAddress(), future.getCause());
						rpcFuture.setException(future.getCause());
					}
				}
			});
		}
		return future;
	}

	public void putWriteHeap(int opcode, int status) {
		this.batchWriteResultHeap.put(opcode, status);
	}
	public int removeAndGetWriteHeap(int opaque) {
		Integer x = batchWriteResultHeap.remove(opaque);
		if (x == null) {
			return -1;
		} else {
			return x;
		}
	}

	public void putReadHeap(int opcode, OcsReplyMessageWrapper c) {
		this.batchReadResultHeap.put(opcode, c);
	}

	public OcsReplyMessageWrapper getReadHeap(int opcode) {
		return this.batchReadResultHeap.get(opcode);
	}

	public OcsReplyMessageWrapper removeAndGetReadHeap(int opcode) {
		return this.batchReadResultHeap.remove(opcode);
	}

	public OcsFutureInternal registCallTask(int opaque) {
		OcsFutureInternal future = new OcsFutureInternal();
		future.setRemoteAddress(destAddress);
		tasks.put(opaque, future);
		return future;
	}

	public OcsFutureInternal getAndRemoveCallTask(int opaque) {
		OcsFutureInternal future = tasks.remove(opaque);
		return future;
	}

	public OcsFutureInternal clearTimeoutCallTask(int opaque) {
		OcsFutureInternal future = tasks.remove(opaque);
		if (future != null) {
			if (future.getOpaques() != null) {
				Collection<Integer> c = future.getOpaques();
				if (future.isAccessWrite()) {
					for (Integer o : c) {
						batchWriteResultHeap.remove(o);
					}
				} else {
					for (Integer o : c) {
						batchReadResultHeap.remove(o);
					}
				}
			}
			future.setException(new OcsException("timeout"));
		}
		return future;
	}

	public boolean waitConnect(long waittime) {
		if (finished)
			return true;
		synchronized (sessionLock) {
			try {
				if (finished == false) {
					if (waittime == 0) {
						sessionLock.wait();
					} else {
						sessionLock.wait(waittime);
					}
				}
			} catch (InterruptedException e) {
				return false;
			}
			return finished;
		}
	}

	public void close() {
		if (channelImpl != null) {
			try {
				channelImpl.close();
			} catch (Exception e) {
			}
		}
	}

	public OcsRpcID getRpcID() {
		return rpcID;
	}

	public void setRpcID(OcsRpcID rpcID) {
		this.rpcID = rpcID;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("OcsChannel: {");
		sb.append(", remote ip: " + (this.channelImpl != null ? this.channelImpl.getRemoteAddress() : "connection not ready"));
		sb.append("}");
		return sb.toString();
	}

	public ConcurrentHashMap<Integer, Integer> getBatchWriteResultHeap() {
		return batchWriteResultHeap;
	}

	public void setBatchWriteResultHeap(ConcurrentHashMap<Integer, Integer> batchWriteResultHeap) {
		this.batchWriteResultHeap = batchWriteResultHeap;
	}

	public ConcurrentHashMap<Integer, OcsReplyMessageWrapper> getBatchReadResultHeap() {
		return batchReadResultHeap;
	}

	public void setBatchReadResultHeap(ConcurrentHashMap<Integer, OcsReplyMessageWrapper> batchReadResultHeap) {
		this.batchReadResultHeap = batchReadResultHeap;
	}

	public void deleteSession() {
		if (this.rpcInstance != null) {
			this.rpcInstance.deleteSession(conn);
		}
	}
}
