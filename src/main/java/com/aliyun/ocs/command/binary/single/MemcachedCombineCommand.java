package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent2ByteArray;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;
import com.aliyun.ocs.util.OcsBuffer;

public class MemcachedCombineCommand extends MemcachedAbstractSingleCommand {
	protected Object value;
	public MemcachedCombineCommand(OcsRpc rpc, byte opcode, String key, Object value) {
		super(rpc, opcode, key);
		this.value = value;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	public BinaryMemcachedMessage buildMessage() throws OcsException {
		byte[] bkey = trans.encodeKey(key);
		OcsBuffer bVal = trans.encodeTo(value);
		if (bkey == null || bkey.length == 0) {
			throw new OcsException("null key");
		}
		if (bVal.getBuffer() == null) {
			throw new OcsException("null value");
		}
		BinaryContent content = new BinaryContent2ByteArray(bkey, bVal.getBuffer());
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) 0, (byte) 0,
				content.getSize(), 0l);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, content);
		message.setOpcode(opcode);
		return message;
	}
}
