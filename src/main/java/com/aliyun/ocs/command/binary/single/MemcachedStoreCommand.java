package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.OcsException;
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

/*
 * Request header:

 PART 1:
 Byte/     0       |       1       |       2       |       3       |
 /              |               |               |               |
 |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 +---------------+---------------+---------------+---------------+
 0| Magic         | Opcode        | Key length                    |
 +---------------+---------------+---------------+---------------+
 4| Extras length | Data type     | Reserved                      |
 +---------------+---------------+---------------+---------------+
 8| Total body length                                             |
 +---------------+---------------+---------------+---------------+
 12| Opaque                                                        |
 +---------------+---------------+---------------+---------------+
 16| CAS                                                           |
 |                                                               |
 +---------------+---------------+---------------+---------------+
 Total 24 bytes

 PART 2:
 Byte/     0       |       1       |       2       |       3       |
 /              |               |               |               |
 |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 +---------------+---------------+---------------+---------------+
 0| Flags                                                         |
 +---------------+---------------+---------------+---------------+
 4| Expiration                                                    |
 +---------------+---------------+---------------+---------------+
 */
/*
 * 
 * Successful add response:

 Byte/     0       |       1       |       2       |       3       |
 /              |               |               |               |
 |0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|0 1 2 3 4 5 6 7|
 +---------------+---------------+---------------+---------------+
 0| 0x81          | 0x02          | 0x00          | 0x00          |
 +---------------+---------------+---------------+---------------+
 4| 0x00          | 0x00          | 0x00          | 0x00          |
 +---------------+---------------+---------------+---------------+
 8| 0x00          | 0x00          | 0x00          | 0x00          |
 +---------------+---------------+---------------+---------------+
 12| 0x00          | 0x00          | 0x00          | 0x00          |
 +---------------+---------------+---------------+---------------+
 16| 0x00          | 0x00          | 0x00          | 0x00          |
 +---------------+---------------+---------------+---------------+
 20| 0x00          | 0x00          | 0x00          | 0x01          |
 +---------------+---------------+---------------+---------------+

 Total 24 bytes

 Field        (offset) (value)
 Magic        (0)    : 0x81
 Opcode       (1)    : 0x02
 Key length   (2,3)  : 0x0000
 Extra length (4)    : 0x00
 Data type    (5)    : 0x00
 Status       (6,7)  : 0x0000
 Total body   (8-11) : 0x00000000
 Opaque       (12-15): 0x00000000
 CAS          (16-23): 0x0000000000000001
 Extras              : None
 Key                 : None
 Value               : None
 */
public class MemcachedStoreCommand extends MemcachedAbstractSingleCommand {
	protected Object value;
	protected int flags;
	protected int exper;
	protected long cas;
	public MemcachedStoreCommand(final OcsRpc rpc, byte opcode, String key, Object value, int flags, int exper, long cas) {
		super(rpc, opcode, key);
		this.value = value;
		this.flags = flags;
		this.exper = exper;
		this.cas = cas;
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
		BinaryExtras extras = new BinaryExtras_Flags_Expire(bVal.getFlag(), exper);
		BinaryContent content = new BinaryContent2ByteArray(bkey, bVal.getBuffer());
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short)  bkey.length, (byte) (extras.getSize()),
				(byte) 0, extras.getSize() + content.getSize(), cas);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, extras, content);
		message.setOpcode(opcode);
		return message;
	}

}
