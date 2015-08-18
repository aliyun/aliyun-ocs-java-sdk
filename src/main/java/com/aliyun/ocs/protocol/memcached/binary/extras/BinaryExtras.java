package com.aliyun.ocs.protocol.memcached.binary.extras;

import org.jboss.netty.buffer.ChannelBuffer;

public interface BinaryExtras {
	public boolean encodeTo(ChannelBuffer out);

	public boolean decodeFrom(ChannelBuffer in);

	public int getSize();

	public void setSize(int size);

	public int getFlags();
}
