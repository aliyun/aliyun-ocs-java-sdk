package com.aliyun.ocs.protocol.memcached.binary.extras;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryExtras;

public class BinaryExtras_Flags_Expire extends AbstractBinaryExtras {
	private int flags;
	private int expr;

	public BinaryExtras_Flags_Expire() {
		this.flags = 0;
		this.expr  = 0;
	}
	public BinaryExtras_Flags_Expire(int flags, int expr) {
		this.flags = flags;
		this.expr = expr;
	}
  
	public int getFlags() {
		return flags;
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeInt(flags);
		out.writeInt(expr);
		return true;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		flags = in.readInt();
		expr = in.readInt();
		return true;
	}

	public int getSize() {
		return 8;
	}

	public void setSize(int size) {
	}

}
