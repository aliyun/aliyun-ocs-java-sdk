package com.aliyun.ocs.command.binary.multi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsKeyValueCollection;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent2ByteArray;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;
import com.aliyun.ocs.util.OcsBuffer;

public class MemcachedBatchCombineCommand extends MemcachedAbstractBatchCommand {
	protected Map<String, OcsKeyValueCollection.OcsValue> keyValues;

	public MemcachedBatchCombineCommand(OcsRpc rpc, byte opcode, OcsKeyValueCollection c) {
		super(rpc, opcode);
		this.keyValues = c.build();
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	public Map<String, BinaryMemcachedMessage> buildMessages() throws OcsException {
		Map<String, BinaryMemcachedMessage> messages = new HashMap<String, BinaryMemcachedMessage>();
		for (Map.Entry<String, OcsKeyValueCollection.OcsValue> entry : keyValues.entrySet()) {
			String key = entry.getKey();
			OcsKeyValueCollection.OcsValue value = entry.getValue();
			byte[] bkey = trans.encodeKey(key);
			OcsBuffer bVal = trans.encodeTo(value.getValue());
			if (bkey == null || bkey.length == 0) {
				throw new OcsException("null key");
			}
			if (bVal.getBuffer() == null) {
				throw new OcsException("null value");
			}
			BinaryContent content = new BinaryContent2ByteArray(bkey, bVal.getBuffer());
			BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) 0, (byte) 0, content.getSize(), 0l);
			BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, content);
			message.setOpcode(opcode);
			messages.put(key, message);
		}
		return messages;
	}

	public Set<String> getKeys() {
		return keyValues.keySet();
	}

	public int dummyStatus() {
		return OcsReplyStatus.REPLY_SUCCESS;
	}
}
