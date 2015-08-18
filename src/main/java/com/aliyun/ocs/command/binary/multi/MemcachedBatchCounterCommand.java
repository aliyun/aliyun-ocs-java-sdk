package com.aliyun.ocs.command.binary.multi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsKeyCounterCollection;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContentByteArray;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras_Amount_Init_Expire;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedBatchCounterCommand extends MemcachedAbstractBatchCommand {
	protected Map<String, OcsKeyCounterCollection.OcsCounter> keyValues;

	public MemcachedBatchCounterCommand(OcsRpc rpc, byte opcode, OcsKeyCounterCollection c) {
		super(rpc, opcode);
		this.keyValues = c.build();
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_LONG;
	}

	public Map<String, BinaryMemcachedMessage> buildMessages() throws OcsException {
		Map<String, BinaryMemcachedMessage> messages = new HashMap<String, BinaryMemcachedMessage>();
		for (Map.Entry<String, OcsKeyCounterCollection.OcsCounter> entry : keyValues.entrySet()) {
			String key = entry.getKey();
			OcsKeyCounterCollection.OcsCounter value = entry.getValue();
			byte[] bkey = trans.encodeKey(key);
			if (bkey == null || bkey.length == 0) {
				throw new OcsException("null key");
			}
			BinaryExtras extras = new BinaryExtras_Amount_Init_Expire(value.getValue(), value.getInit(), value.getExper());
			BinaryContent content = new BinaryContentByteArray(bkey);
			BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) extras.getSize(),
					(byte) 0, extras.getSize() + content.getSize(), 0l);
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
