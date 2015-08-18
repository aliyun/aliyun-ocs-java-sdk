package com.aliyun.ocs.rpc;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;

public class OcsFrameDecoder extends FrameDecoder {
	Log logger = LogFactory.getLog(OcsFrameDecoder.class);
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws OcsException {
		OcsChannel ocsChannel = (OcsChannel) channel.getAttachment();
		OcsReplyMessageWrapper wrapper = ocsChannel.getCachedRpcMessage();
		if (wrapper == null) {
			if (buffer.readableBytes() < BinaryMemcachedMessageHeader.HEADER_SIZE) {
				return null;
			}
			// construct header first
			wrapper = ocsChannel.getPacketFactory().buildFromReply(buffer);
		}
		if (wrapper != null && logger.isDebugEnabled()) {
			logger.debug("Recieve response: {Opcode: " + wrapper.getOpcode() + ", Opaque: " + wrapper.getOpaque() + "}, " + ocsChannel.toString());
		}
		if (buffer.readableBytes() < wrapper.getBodySize()) {
			ocsChannel.setCachedRpcMessage(wrapper);
			return null;
		} else {
			ocsChannel.setCachedRpcMessage(null);
		}
		wrapper.assignBodyBuffer(buffer);
		return wrapper;

	}

}
