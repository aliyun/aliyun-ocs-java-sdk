package com.aliyun.ocs.protocol.memcached.binary.content;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryContent;

public class BinaryContentByteArray extends AbstractBinaryContent {
	private byte[] v;
	private int size;

	public BinaryContentByteArray(byte[] v) {
		this.v = v;
	}

	public BinaryContentByteArray() {
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeBytes(v);
		return true;
	}

	public boolean decodeFrom(ChannelBuffer out) {
		v = new byte[size];
		out.readBytes(v);
		return true;
	}

	public int getSize() {
		return v.length;
	}

	public byte[] getByteArray() {
		return v;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
