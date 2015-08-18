package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;

public abstract class AbstractBinaryExtras implements BinaryExtras {
	public boolean encodeTo(ChannelBuffer out) {
		return false;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		return false;
	}

	public int getFlags() {
		return -1;
	}
}
