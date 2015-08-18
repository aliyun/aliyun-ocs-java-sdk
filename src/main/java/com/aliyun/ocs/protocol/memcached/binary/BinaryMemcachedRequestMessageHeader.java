package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

public class BinaryMemcachedRequestMessageHeader extends AbstractBinaryMemcachedMessageHeader {

	public BinaryMemcachedRequestMessageHeader(short keyLength, byte extrasLength, byte dataType, int totalBodyLength, long cas) {
		super(keyLength, extrasLength, dataType, totalBodyLength, cas);
		this.magic = (byte) BinaryMemcachedMessageHeader.PROTOCOL_BINARY_REQUEST_MAGIC;
	}

	public BinaryMemcachedRequestMessageHeader() {
		this.magic = (byte) BinaryMemcachedMessageHeader.PROTOCOL_BINARY_REQUEST_MAGIC;
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeByte(magic);
		out.writeByte(opcode);
		out.writeShort(keyLength);
		out.writeByte(extrasLength);
		out.writeByte(dataType);
		out.writeShort(0/* reserved */);
		out.writeInt(totalBodyLength);
		out.writeInt(opaque);
		out.writeLong(cas);
		return true;
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
