package com.aliyun.ocs.protocol.memcached.binary.content;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryContent;

public class BinaryContentLong extends AbstractBinaryContent {
	private long v1;

	public BinaryContentLong() {
	}

	public boolean decodeFrom(ChannelBuffer in) {
		v1 = in.readLong();
		return true;
	}

	public int getSize() {
		return 8;
	}

	public void setSize(int size) {
	}

	public long getLong() {
		return v1;
	}

}
