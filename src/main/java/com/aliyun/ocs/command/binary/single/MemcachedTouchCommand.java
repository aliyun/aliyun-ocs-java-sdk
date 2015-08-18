package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
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

public class MemcachedTouchCommand extends MemcachedAbstractSingleCommand {
	protected int exper;

	public MemcachedTouchCommand(OcsRpc rpc, byte opcode, String key, int exper) {
		super(rpc, opcode, key);
		this.exper = exper;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	public BinaryMemcachedMessage buildMessage() throws OcsException {
		byte[] bkey = trans.encodeKey(key);
		if (bkey == null || bkey.length == 0) {
			throw new OcsException("null key");
		}
		BinaryExtras extras = new BinaryExtras_Flags(exper);
		BinaryContent content = new BinaryContentByteArray(bkey);
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bkey.length, (byte) (extras.getSize()),
				(byte) 0, extras.getSize() + content.getSize(), 0l);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, content);
		message.setOpcode(opcode);
		return message;
	}
}
