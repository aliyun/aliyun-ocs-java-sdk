package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

public interface BinaryMemcachedMessageHeader {
	public static int HEADER_SIZE = 24;
	public static byte PROTOCOL_BINARY_REQUEST_MAGIC = (byte) 0x80;
	public static byte PROTOCOL_BINARY_REPLY_MAGIC = (byte) 0x81;

	public boolean encodeTo(ChannelBuffer out);

	public boolean decodeFrom(ChannelBuffer in);

	public boolean existExtras();

	public boolean existContent();

	public int getOpCode();

	public int getStatus();

	public int getBodySize();

	public int getExtrasSize();

	public int getContentSize();

	public int getKeySize();

	public void setOpaque(int opaque);

	public void setOpcode(byte opcode);

	public int getOpaque();
	
	public long getCas();
}
