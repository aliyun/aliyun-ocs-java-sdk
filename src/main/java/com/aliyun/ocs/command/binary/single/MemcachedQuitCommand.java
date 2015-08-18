package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedQuitCommand extends MemcachedNoopCommand {

	public MemcachedQuitCommand(OcsRpc rpc, byte opcode) {
		super(rpc, opcode, "quit");
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

}
