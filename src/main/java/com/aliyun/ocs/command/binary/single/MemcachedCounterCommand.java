package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
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
import com.aliyun.ocs.util.OcsBuffer;

/*
 Extra data for incr/decr:

 Byte/     0       |       1       |       2       |       3       |
 /              |               |               |               |
 |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 +---------------+---------------+---------------+---------------+
 0| Amount to add                                                 |
 |                                                               |
 +---------------+---------------+---------------+---------------+
 8| Initial value                                                 |
 |                                                               |
 +---------------+---------------+---------------+---------------+
 16| Expiration                                                    |
 +---------------+---------------+---------------+---------------+
 Total 20 bytes

 These commands will either add or remove the specified amount to the
 requested counter.

 If the counter does not exist, one of two things may happen:

 1.  If the expiration value is all one-bits (0xffffffff), the
 operation will fail with NOT_FOUND.
 2.  For all other expiration values, the operation will succeed by
 seeding the value for this key with the provided initial value to
 expire with the provided expiration time.  The flags will be set
 to zero.
 */
public class MemcachedCounterCommand extends MemcachedAbstractSingleCommand {
	protected long amount;
	protected long init;
	protected int expir;
	public MemcachedCounterCommand(OcsRpc rpc, byte opcode, String key, long amount, long init, int expir) {
		super(rpc, opcode, key);
		this.amount = amount;
		this.init = init;
		this.expir = expir;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_LONG;
	}

	public BinaryMemcachedMessage buildMessage() throws OcsException {
		OcsBuffer bKey = trans.encodeTo(key);
		if (bKey.getBuffer() == null || bKey.getLength() == 0) {
			throw new OcsException("null key");
		}
		BinaryExtras extras = new BinaryExtras_Amount_Init_Expire(this.amount, this.init, this.expir);
		BinaryContent content = new BinaryContentByteArray(bKey.getBuffer());
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) bKey.getLength(), (byte) extras.getSize(),
				(byte) 0, extras.getSize() + content.getSize(), 0l);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, content);
		message.setOpcode(opcode);
		return message;
	}
}
