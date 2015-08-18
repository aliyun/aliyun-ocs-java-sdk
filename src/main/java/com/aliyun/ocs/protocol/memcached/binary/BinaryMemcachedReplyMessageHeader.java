package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

public class BinaryMemcachedReplyMessageHeader extends AbstractBinaryMemcachedMessageHeader {
	public BinaryMemcachedReplyMessageHeader(byte opcode, short keyLength, byte extrasLength, byte dataType, int totalBodyLength,
			int opaque, long cas) {
		super(keyLength, extrasLength, dataType, totalBodyLength, cas);
		this.magic = (byte) BinaryMemcachedMessageHeader.PROTOCOL_BINARY_REPLY_MAGIC;
	}

	public BinaryMemcachedReplyMessageHeader() {
		this.magic = (byte) BinaryMemcachedMessageHeader.PROTOCOL_BINARY_REPLY_MAGIC;
	}

	protected short status;

	public boolean decodeFrom(ChannelBuffer in) {
		magic = in.readByte();
		opcode = in.readByte();
		keyLength = in.readShort();
		extrasLength = in.readByte();
		dataType = in.readByte();
		status = in.readShort();
		totalBodyLength = in.readInt();
		opaque = in.readInt();
		cas = in.readLong();
		return true;
	}

	public int getStatus() {
		return (int)status;
	}

	public int getKeySize() {
		return this.keyLength;
	}

	public void setOpaque(int opaque) {
		this.opaque = opaque;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public int getOpaque() {
		return this.opaque;
	}

}
