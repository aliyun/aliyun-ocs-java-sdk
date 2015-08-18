package com.aliyun.ocs.protocol.memcached.binary.content;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryContent;

public class BinaryContent2ByteArray extends AbstractBinaryContent {
	private byte[] v1;
	private byte[] v2;
	private int keySize;
	private int totalSize;

	public BinaryContent2ByteArray(byte[] v1, byte[] v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public BinaryContent2ByteArray() {
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeBytes(v1);
		out.writeBytes(v2);
		return true;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		int allocKeySize = keySize;
		int allocValueSize = totalSize - keySize;

		v1 = new byte[allocKeySize];
		in.readBytes(v1);

		v2 = new byte[allocValueSize];
		in.readBytes(v2);
		return true;
	}

	public int getSize() {
		return v1.length + v2.length;
	}

	public byte[] getByteArray1() {
		return v1;
	}

	public byte[] getByteArray2() {
		return v2;
	}

	public void setSize(int size) {
		this.totalSize = size;
	}

	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}
}
