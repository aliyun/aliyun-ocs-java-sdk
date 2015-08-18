package com.aliyun.ocs.protocol.memcached.binary;

import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;

public class BinaryMemcachedReplyMessage extends AbstractBinaryMemcachedMessage {

	public BinaryMemcachedReplyMessage(BinaryMemcachedMessageHeader header, BinaryExtras extras, BinaryContent content) {
		super(header, extras, content);
	}

}
