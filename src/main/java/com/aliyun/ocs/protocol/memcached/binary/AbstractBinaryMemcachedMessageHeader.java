package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsReplyStatus;

public abstract class AbstractBinaryMemcachedMessageHeader implements BinaryMemcachedMessageHeader {
	protected byte magic;
	protected byte opcode;
	protected short keyLength;
	protected byte extrasLength;
	protected byte dataType;
	protected int totalBodyLength;
	protected int opaque;
	protected long cas;

	public AbstractBinaryMemcachedMessageHeader() {

	}

	public AbstractBinaryMemcachedMessageHeader(short keyLength, byte extrasLength, byte dataType, int totalBodyLength, long cas) {
		this.keyLength = keyLength;
		this.extrasLength = extrasLength;
		this.dataType = dataType;
		this.totalBodyLength = totalBodyLength;
		this.cas = cas;
	}

	public boolean encodeTo(ChannelBuffer out) {
		return false;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		return false;
	}

	public boolean existExtras() {
		return extrasLength > 0;
	}

	public boolean existContent() {
		return totalBodyLength - extrasLength > 0;
	}

	public int getOpCode() {
		return opcode;
	}

	public int getStatus() {
		return OcsReplyStatus.REPLY_SUCCESS;
	}

	public int getBodySize() {
		return this.totalBodyLength;
	}

	public int getExtrasSize() {
		return this.extrasLength;
	}

	public int getContentSize() {
		return this.totalBodyLength - this.extrasLength;
	}

	public long getCas() {
		return this.cas;
	}
}
