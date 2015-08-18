package com.aliyun.ocs.command.binary.multi;

import java.util.Map;

import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedBatchGATKCommand extends MemcachedBatchGATCommand {
	public MemcachedBatchGATKCommand(OcsRpc rpc, byte opcode, Map<String, Integer> keyValues) {
		super(rpc, opcode, keyValues);
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_2BYTEARRAY;
	}

}
