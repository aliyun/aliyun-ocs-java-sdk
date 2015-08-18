package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContentByteArray;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedGetCommand extends MemcachedAbstractSingleCommand {
	public MemcachedGetCommand(OcsRpc rpc, byte opcode, String key) {
		super(rpc, opcode, key);
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_BYTEARRAY;
	}

	public BinaryMemcachedMessage buildMessage() throws OcsException {
		byte[] bkey = trans.encodeKey(key);
		if (bkey == null || bkey.length == 0) {
			throw new OcsException("null key");
		}
		BinaryContent content = new BinaryContentByteArray(bkey);
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) 0, (byte) 0,
				content.getSize(), 0l);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, content);
		message.setOpcode(opcode);
		return message;
	}
}
