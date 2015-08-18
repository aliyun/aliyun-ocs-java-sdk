package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras_Flags;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedFlushCommand extends MemcachedAbstractSingleCommand {
	protected int exper;

	public MemcachedFlushCommand(OcsRpc rpc, byte opcode, int exper) {
		super(rpc, opcode, "flush");
		this.exper = exper;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_LONG;
	}

	public BinaryMemcachedMessage buildMessage() {
		BinaryExtras extras = new BinaryExtras_Flags(exper);
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) 0, (byte) extras.getSize(), (byte) 0,
				extras.getSize(), 0l);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, null);
		message.setOpcode(opcode);
		return message;
	}
}
