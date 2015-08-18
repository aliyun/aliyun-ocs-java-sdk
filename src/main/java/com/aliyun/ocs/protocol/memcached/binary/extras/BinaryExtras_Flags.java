package com.aliyun.ocs.protocol.memcached.binary.extras;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryExtras;

public class BinaryExtras_Flags extends AbstractBinaryExtras {
	private int flags;

	public BinaryExtras_Flags() {

	}

	public BinaryExtras_Flags(int flags) {
		this.flags = flags;
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeInt(flags);
		return true;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		flags = in.readInt();
		return true;
	}

	public int getSize() {
		return 4;
	}

	public void setSize(int size) {
	}


	public int getFlags() {
		return this.flags;
	}
}
