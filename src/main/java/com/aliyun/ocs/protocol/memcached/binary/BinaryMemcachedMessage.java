package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;

public interface BinaryMemcachedMessage {
	public boolean encodeTo(ChannelBuffer out);

	public boolean decodeFrom(ChannelBuffer in);

	public int size();

	public BinaryMemcachedMessageHeader getHeader();

	public BinaryExtras getExtras();

	public BinaryContent getContent();

	public void setBinaryExtras(BinaryExtras extras);

	public void setBinaryContent(BinaryContent content);

	public int getBodySize();

	public void setOpaque(int opaque);

	public void setOpcode(byte opcode);
	
	public byte getOpcode();

	public int getOpaque();
}
