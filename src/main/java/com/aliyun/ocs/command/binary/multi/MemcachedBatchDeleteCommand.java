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
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedBatchDeleteCommand extends MemcachedAbstractBatchCommand {
	protected Set<String> keys;

	public MemcachedBatchDeleteCommand(OcsRpc rpc, byte opcode, Set<String> keys) {
		super(rpc, opcode);
		this.keys = keys;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_INTERGER_BYTEARRAY;
	}

	public Set<String> getKeys() {
		return this.keys;
	}

	public Map<String, BinaryMemcachedMessage> buildMessages() throws OcsException {
		Map<String, BinaryMemcachedMessage> messages = new HashMap<String, BinaryMemcachedMessage>();
		for (String key : keys) {
			byte[] bkey = trans.encodeKey(key);
			if (bkey == null || bkey.length == 0) {
				throw new OcsException("null key");
			}
			BinaryContent content = new BinaryContentByteArray(bkey);
			BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) (0), (byte) 0, content.getSize(), 0l);
			BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, content);
			message.setOpcode(opcode);
			messages.put(key, message);
		}
		return messages;
	}

	public int dummyStatus() {
		return OcsReplyStatus.REPLY_SUCCESS;
	}

}
