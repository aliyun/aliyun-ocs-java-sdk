package com.aliyun.ocs.protocol.memcached.binary.content;

import org.jboss.netty.buffer.ChannelBuffer;

public interface BinaryContent {
	public boolean encodeTo(ChannelBuffer out);

	public boolean decodeFrom(ChannelBuffer in);

	public int getSize();

	public void setSize(int size);
}
