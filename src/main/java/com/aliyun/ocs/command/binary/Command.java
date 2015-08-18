package com.aliyun.ocs.command.binary;

import java.util.Map;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;

public interface Command {
	public OcsLazyDecoder getLazyDecoder();

	public BinaryMemcachedMessage buildMessage() throws OcsException;

	public Map<String, BinaryMemcachedMessage> buildMessages() throws OcsException;

	public String toString();
}
