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
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras_Flags_Expire;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;
import com.aliyun.ocs.util.OcsBuffer;

public class MemcachedBatchStoreCommand extends MemcachedAbstractBatchCommand {
	protected Map<String, OcsKeyValueCollection.OcsValue> keyValues;

	public MemcachedBatchStoreCommand(OcsRpc rpc, byte opcode, OcsKeyValueCollection c) {
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
			BinaryExtras extras = new BinaryExtras_Flags_Expire(bVal.getFlag(), value.getExper());
			BinaryContent content = new BinaryContent2ByteArray(bkey, bVal.getBuffer());
			BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) (extras.getSize()), (byte) 0, extras.getSize() + content.getSize(), value.getCas());
			BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, content);
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
