package com.aliyun.ocs.protocol.memcached.binary.lazydecoder;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent2ByteArray;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContentByteArray;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras_Flags;
import com.aliyun.ocs.util.OcsBuffer;

public class OcsLazyDecoderInteger2ByteArray implements OcsLazyDecoder {
	public BinaryMemcachedMessage lazyDecode(ChannelBuffer body, BinaryMemcachedMessageHeader header) {
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, null);
		if (header.existExtras()) {
			BinaryExtras extras = new BinaryExtras_Flags();
			extras.setSize(header.getExtrasSize());
			message.setBinaryExtras(extras);
		}
		if (header.existContent()) {
			BinaryContent2ByteArray content = new BinaryContent2ByteArray();
			content.setSize(header.getContentSize());
			content.setKeySize(header.getKeySize());
			message.setBinaryContent(content);
		}
		message.decodeFrom(body);
		return message;
	}

	public  OcsResult lazyConstruct(BinaryMemcachedMessage message, String key, OcsTranscoder trans) throws OcsException {
		BinaryMemcachedMessageHeader header = message.getHeader();
		int flags = message.getExtras().getFlags();
		int status = header.getStatus();
		if (status == OcsReplyStatus.REPLY_SUCCESS) {
			byte[] rvalue = null;
			BinaryContent c = message.getContent();
			if (c instanceof BinaryContent2ByteArray) {
				BinaryContent2ByteArray cc = (BinaryContent2ByteArray) c;
				rvalue = cc.getByteArray1();
				byte[] rkey = cc.getByteArray2();
				return new OcsResult(trans.decodeFrom(new OcsBuffer(rvalue, flags)), trans.decodeKey(rkey), header.getCas(), status);
			} else if (c instanceof BinaryContentByteArray) {
				BinaryContentByteArray cc = (BinaryContentByteArray) c;
				rvalue = cc.getByteArray();
				return new OcsResult(trans.decodeFrom(new OcsBuffer(rvalue, flags)), key, header.getCas(), status);
			}
		}
		return new OcsResult(null, key, status);
	}

	public  OcsResult lazyConstruct(int status, String key, OcsTranscoder trans) {
		return new OcsResult(null, key, status);
	}

}
