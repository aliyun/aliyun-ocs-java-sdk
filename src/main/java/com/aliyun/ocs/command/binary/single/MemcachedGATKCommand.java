package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedGATKCommand extends MemcachedGetKCommand {
	public MemcachedGATKCommand(OcsRpc rpc, byte opcode, String key) {
		super(rpc, opcode, key);
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_2BYTEARRAY;
	}
}
