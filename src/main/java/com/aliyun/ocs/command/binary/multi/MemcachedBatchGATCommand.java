package com.aliyun.ocs.command.binary.multi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContentByteArray;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras_Flags;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedBatchGATCommand extends MemcachedAbstractBatchCommand {
	protected Map<String, Integer> keyExpers;

	public MemcachedBatchGATCommand(OcsRpc rpc, byte opcode, Map<String, Integer> keyExpers) {
		super(rpc, opcode);
		this.keyExpers = keyExpers;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_BYTEARRAY;
	}

	public Map<String, BinaryMemcachedMessage> buildMessages() throws OcsException {
		Map<String, BinaryMemcachedMessage> messages = new HashMap<String, BinaryMemcachedMessage>();
		for (Map.Entry<String, Integer> entry : keyExpers.entrySet()) {
			String key = entry.getKey();
			int exper = entry.getValue();
			byte[] bkey = trans.encodeKey(key);
			if (bkey == null || bkey.length == 0) {
				throw new OcsException("null key");
			}
			BinaryContent content = new BinaryContentByteArray(bkey);
			BinaryExtras extras = new BinaryExtras_Flags(exper);
			BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) extras.getSize(), (byte) 0, content.getSize(), 0l);
			BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, content);
			message.setOpcode(opcode);
			messages.put(key, message);
		}
		return messages;
	}

	public Set<String> getKeys() {
		return keyExpers.keySet();
	}

	public int dummyStatus() {
		return OcsReplyStatus.REPLY_ERROR_KEY_NOT_FOUND;
	}

}
