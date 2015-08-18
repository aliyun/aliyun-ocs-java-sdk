package com.aliyun.ocs.command.binary.multi;

import java.util.Set;

import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedBatchGetKCommand extends MemcachedBatchGetCommand {

	public MemcachedBatchGetKCommand(OcsRpc rpc, byte opcode, Set<String> keys) {
		super(rpc, opcode, keys);
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_2BYTEARRAY;
	}

}
