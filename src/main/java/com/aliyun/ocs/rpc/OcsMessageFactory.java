package com.aliyun.ocs.rpc;

import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.command.binary.Command;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedReplyMessageHeader;


public class OcsMessageFactory {
	private static OcsMessageFactory instance = new OcsMessageFactory();

	public static OcsMessageFactory getOcsMessageFactoryInstance() {
		return instance;
	}

	public  BinaryMemcachedMessage buildFromRequest(Command command) throws OcsException {
		return command.buildMessage();
	}

	public Map<String, BinaryMemcachedMessage> buildFromBatchRequest(Command command) throws OcsException {
		return command.buildMessages();
	}

	public OcsReplyMessageWrapper buildFromReply(ChannelBuffer in) throws OcsException {
		BinaryMemcachedMessageHeader header = new BinaryMemcachedReplyMessageHeader();
		header.decodeFrom(in);
		return new OcsReplyMessageWrapper(header);
	}
}
