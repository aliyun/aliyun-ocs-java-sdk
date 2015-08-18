package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedNoopCommand extends MemcachedAbstractSingleCommand {
	 
	public MemcachedNoopCommand(OcsRpc rpc, byte opcode) {
		super(rpc, opcode,"noop");
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	public MemcachedNoopCommand(OcsRpc rpc, byte opcode, String commandName) {
		super(rpc, opcode, commandName);
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	public BinaryMemcachedMessage buildMessage() {
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) 0, (byte) 0, (byte) 0, 0, 0);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, null);
		message.setOpcode(opcode);
		return message;
	}
}
