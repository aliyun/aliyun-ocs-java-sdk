package com.aliyun.ocs.protocol.memcached.binary.lazydecoder;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;

public interface OcsLazyDecoder {
	public BinaryMemcachedMessage lazyDecode(ChannelBuffer body, BinaryMemcachedMessageHeader header);

	public  OcsResult lazyConstruct(BinaryMemcachedMessage message, String key, OcsTranscoder trans) throws OcsException;

	public  OcsResult lazyConstruct(int status, String key, OcsTranscoder trans);
}
