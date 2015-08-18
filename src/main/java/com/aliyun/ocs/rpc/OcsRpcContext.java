package com.aliyun.ocs.rpc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;

import com.aliyun.ocs.protocol.memcached.binary.MemcachedOpCode;
import com.aliyun.ocs.rpc.future.OcsFutureInternal;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;

public class OcsRpcContext {
	private static int TIMEOUT_COUNT_PER_LOG = 100;
	private Log logger = LogFactory.getLog(OcsRpcContext.class);
	private OcsTimeoutBackgrand backgrandRef;
	private AtomicInteger timeoutCounter = new AtomicInteger(0);

	public OcsRpcContext(OcsTimeoutBackgrand backgrand) {
		this.backgrandRef = backgrand;
	}

	public void messageReceived(Channel channel, OcsReplyMessageWrapper messageWrapper) throws Exception {

		OcsChannel ocsChannel = OcsChannel.getOcsChannel(channel);
		if (logger.isDebugEnabled()) {
			logger.debug("Received MSG from " + ocsChannel.toString() + ", Opqaue " + messageWrapper.getOpaque() + ", opcode: " + messageWrapper.getOpcode());
			;
		}
		 
		int opcode = messageWrapper.getOpcode();
		if (opcode == MemcachedOpCode.ADDQ ||
			opcode == MemcachedOpCode.APPENDQ ||
			opcode == MemcachedOpCode.DELETEQ ||
			opcode == MemcachedOpCode.FLUSHQ ||
			opcode == MemcachedOpCode.PREPENDQ ||
			opcode == MemcachedOpCode.QUITQ ||
			opcode == MemcachedOpCode.PREPENDQ ||
			opcode == MemcachedOpCode.FLUSHQ ||
			opcode == MemcachedOpCode.REPLACEQ) {
			ocsChannel.putWriteHeap(messageWrapper.getOpaque(), messageWrapper.getStatus());
		} else if (opcode == MemcachedOpCode.DECREMENTQ ||
				opcode == MemcachedOpCode.INCREMENTQ ||
				opcode == MemcachedOpCode.GATKQ ||
				opcode == MemcachedOpCode.GATQ ||
				opcode == MemcachedOpCode.GETQ) {
			ocsChannel.putReadHeap(messageWrapper.getOpaque(), messageWrapper);	
		} else {
			OcsFutureInternal future = ocsChannel.getAndRemoveCallTask(messageWrapper.getOpaque());
			backgrandRef.removeRpcID(ocsChannel.getRpcID());
			if (future == null) {
				// TIMEOUT, log the message
				if (timeoutCounter.incrementAndGet() > TIMEOUT_COUNT_PER_LOG) {
					timeoutCounter.set(0);
					OcsRpcID rpcID = ocsChannel.getRpcID();
					logger.error("rpc timeout, remote ip: " + rpcID.getSession().getDestAddress() + ", delay: " + rpcID.getDelay(TimeUnit.MICROSECONDS));
				}
				if (logger.isDebugEnabled()) {
					OcsRpcID rpcID = ocsChannel.getRpcID();
					logger.error("rpc timeout, remote ip: " + rpcID.getSession().getDestAddress() + ", delay: " + rpcID.getDelay(TimeUnit.MICROSECONDS));
				}
				return;
			}
			future.setValue(messageWrapper);
		}
	}

	public void exceptionCaught(Channel channel, Throwable cause) throws Exception {
		OcsChannel ocsChannel = OcsChannel.getOcsChannel(channel);
		if (ocsChannel != null) {
			ocsChannel.deleteSession();
		}
		logger.error("connection exception, remote: " + channel.getRemoteAddress(), cause);
	}

	public void channelDisconnected(Channel channel) {
		OcsChannel ocsChannel = OcsChannel.getOcsChannel(channel);
		if (ocsChannel != null) {
			ocsChannel.deleteSession();
		}
		//connectionRef.removeChannel();
		logger.error("connection disconnected, remote: " + channel.getRemoteAddress());
	}

}
