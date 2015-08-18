package com.aliyun.ocs.protocol.memcached.binary.lazydecoder;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;

public class OcsLazyDeocoderNoneNone implements OcsLazyDecoder {
	public BinaryMemcachedMessage lazyDecode(ChannelBuffer body, BinaryMemcachedMessageHeader header) {
		return new AbstractBinaryMemcachedMessage(header, null, null);
	}

	public  OcsResult lazyConstruct(BinaryMemcachedMessage message, String key, OcsTranscoder trans) {
		return new OcsResult(null, key, message.getHeader().getStatus());
	}

	public  OcsResult lazyConstruct(int status, String key, OcsTranscoder trans) {
		return new OcsResult(null, key, status);
	}
}
