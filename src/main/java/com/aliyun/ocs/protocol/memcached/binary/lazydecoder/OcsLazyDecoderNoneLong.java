package com.aliyun.ocs.protocol.memcached.binary.lazydecoder;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContentLong;

public class OcsLazyDecoderNoneLong implements OcsLazyDecoder {
	public BinaryMemcachedMessage lazyDecode(ChannelBuffer body, BinaryMemcachedMessageHeader header) {
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, null);
		if (header.existContent()) {
			BinaryContent content = new BinaryContentLong();
			content.setSize(header.getContentSize());
			message.setBinaryContent(content);
		}
		message.decodeFrom(body);
		return message;
	}

	public OcsResult lazyConstruct(BinaryMemcachedMessage message, String key, OcsTranscoder trans) {
		BinaryMemcachedMessageHeader header = message.getHeader();
		int status = header.getStatus();
		if (status == OcsReplyStatus.REPLY_SUCCESS) {
			BinaryContent c = message.getContent();
			if (c instanceof BinaryContentLong) {
				BinaryContentLong cc = (BinaryContentLong) c;
				return new OcsResult(cc.getLong(), key, header.getCas(), status);
			}
		}
		return new OcsResult(null, key, status);
	}

	public OcsResult lazyConstruct(int status, String key, OcsTranscoder trans) {
		return new OcsResult(null, key, status);
	}
}
