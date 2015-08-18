package com.aliyun.ocs.protocol.memcached.binary;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aliyun.ocs.protocol.memcached.binary.content.BinaryContent;
import com.aliyun.ocs.protocol.memcached.binary.extras.BinaryExtras;

public class AbstractBinaryMemcachedMessage implements BinaryMemcachedMessage {
	BinaryMemcachedMessageHeader header = null;
	BinaryExtras extras = null;
	BinaryContent content = null;

	public AbstractBinaryMemcachedMessage(BinaryMemcachedMessageHeader header, BinaryExtras extras, BinaryContent content) {
		this.header = header;
		this.extras = extras;
		this.content = content;
	}

	public boolean encodeTo(ChannelBuffer out) {
		header.encodeTo(out);
		if (extras != null) {
			extras.encodeTo(out);
		}
		if (content != null) {
			content.encodeTo(out);
		}
		return false;
	}

	public boolean decodeFrom(ChannelBuffer in) {
		if (extras != null) {
			extras.setSize(header.getExtrasSize());
			extras.decodeFrom(in);
		}
		if (content != null) {
			content.setSize(header.getContentSize());
			content.decodeFrom(in);
		}
		return false;
	}

	public int size() {
		int res = 24;
		if (extras != null) {
			res += extras.getSize();
		}
		if (content != null) {
			res += content.getSize();
		}
		return res;
	}

	public BinaryMemcachedMessageHeader getHeader() {
		return header;
	}

	public BinaryExtras getExtras() {
		return extras;
	}

	public BinaryContent getContent() {
		return content;
	}

	public void setBinaryExtras(BinaryExtras extras) {
		this.extras = extras;
	}

	public void setBinaryContent(BinaryContent content) {
		this.content = content;
	}

	public int getBodySize() {
		return header.getBodySize();
	}

	public void setOpaque(int opaque) {
		header.setOpaque(opaque);
	}

	public void setOpcode(byte opcode) {
		header.setOpcode(opcode);
	}

	public int getOpaque() {
		return header.getOpaque();
	}

	public byte getOpcode() {
		return (byte)header.getOpCode();
	}
}
