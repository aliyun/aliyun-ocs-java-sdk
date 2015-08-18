package com.aliyun.ocs.protocol.memcached.binary.extras;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryExtras;

public class BinaryExtras_Amount_Init_Expire extends AbstractBinaryExtras {
	private long amount;
	private long init;
	private int expir;

	public BinaryExtras_Amount_Init_Expire(long amount, long init, int expir) {
		this.amount = amount;
		this.init = init;
		this.expir = expir;
	}

	public boolean encodeTo(ChannelBuffer out) {
		out.writeLong(amount);
		out.writeLong(init);
		out.writeInt(expir);
		return true;
	}
	public boolean decodeFrom(ChannelBuffer in) {
		amount = in.readLong();
		init = in.readLong();
		expir = in.readInt();
		return true;
	}

	public int getSize() {
		return 20;
	}

	public void setSize(int size) {
	}
}
