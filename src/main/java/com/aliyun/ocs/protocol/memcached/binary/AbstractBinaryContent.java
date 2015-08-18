package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;

public abstract class AbstractBinaryContent implements BinaryContent {

	public boolean encodeTo(ChannelBuffer out) {
		return false;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		return false;
	}
}
