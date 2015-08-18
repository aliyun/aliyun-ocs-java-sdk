package com.aliyun.ocs.rpc;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;

public class OcsReplyMessageWrapper {
	private ChannelBuffer lazyBody = null;
	private BinaryMemcachedMessageHeader header;
	BinaryMemcachedMessage message = null;

	public OcsReplyMessageWrapper(BinaryMemcachedMessageHeader header) {
		this.header = header;
	}

	public void lazyDecode(OcsLazyDecoder lazyDecoder) throws OcsException {
		if (lazyBody != null) {
			if (lazyDecoder == null) {
				throw new OcsException("Internal Error, Opcode: " + header.getOpCode());
			}
			message = lazyDecoder.lazyDecode(lazyBody, header);
		}
	}

	public BinaryMemcachedMessage getMessage() {
		return this.message;
	}

	public void setLazyBody(ChannelBuffer lazyBody) {
		this.lazyBody = lazyBody;
	}

	public void assignBodyBuffer(ChannelBuffer in) {
		if (in.readableBytes() < getBodySize()) {
			return;
		}
		lazyBody = in.readSlice(getBodySize());
	}

	public int getOpaque() {
		return header.getOpaque();
	}

	public boolean hasReply() {
		return lazyBody != null;
	}

	public int getBodySize() {
		return header.getBodySize();
	}
	public byte getOpcode() {
		return (byte)header.getOpCode();
	}
	public int getStatus() {
		return header.getStatus();
	}
}
