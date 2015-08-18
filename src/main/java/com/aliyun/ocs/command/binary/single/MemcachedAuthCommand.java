package com.aliyun.ocs.command.binary.single;

import com.aliyun.ocs.protocol.memcached.binary.AbstractBinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedRequestMessageHeader;
import com.aliyun.ocs.protocol.memcached.binary.MemcachedOpCode;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent2ByteArray;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.LazyDecoderFactory;
import com.aliyun.ocs.rpc.OcsRpc;

public class MemcachedAuthCommand extends MemcachedAbstractSingleCommand {

	public MemcachedAuthCommand(OcsRpc rpc, byte[] memchanism, byte[] eveluate) {
		super(rpc, MemcachedOpCode.SASL_AUTH, "auth");
		this.memchanism = memchanism;
		this.eveluate = eveluate;
		this.lazyDecoder = LazyDecoderFactory.LAZY_DECODER_NONE_NONE;
	}

	protected byte[] memchanism;
	protected byte[] eveluate;

	public BinaryMemcachedMessage buildMessage() {
		BinaryContent content = new BinaryContent2ByteArray(memchanism, eveluate);
		BinaryMemcachedMessageHeader header = new BinaryMemcachedRequestMessageHeader((short) memchanism.length, (byte) 0, (byte) 0,
				content.getSize(), 0);
		BinaryMemcachedMessage message = new AbstractBinaryMemcachedMessage(header, null, content);
		message.setOpcode(MemcachedOpCode.SASL_AUTH);
		return message;
	}

}
