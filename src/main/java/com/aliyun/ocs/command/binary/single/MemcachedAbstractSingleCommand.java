package com.aliyun.ocs.command.binary.single;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;
import com.aliyun.ocs.rpc.OcsRpc;

public abstract class MemcachedAbstractSingleCommand implements MemcachedSingleCommand {
	protected final OcsRpc rpc;
	protected byte opcode;
	protected String key;
	protected OcsLazyDecoder lazyDecoder;
	protected OcsTranscoder trans;

	public MemcachedAbstractSingleCommand(final OcsRpc rpc, byte opcode, String key) {
		this.rpc = rpc;
		this.opcode = opcode;
		this.key = key;
	}

	public OcsResult syncExecute(OcsOptions options) {
		if (options == null) {
			options = OcsOptions.defaultOptions();
		}
		OcsFuture<OcsResult> future = null;
		try {
			future = execute(options);
			return future.get();
		} catch (InterruptedException e) {
			return new OcsResult(null, key, OcsReplyStatus.REPLY_ERROR_TMPFAIL, e.getMessage());
		} catch (ExecutionException e) {
			return new OcsResult(null, key, OcsReplyStatus.REPLY_ERROR_TMPFAIL, e.getMessage());
		} catch (OcsException e) {
			return new OcsResult(null, key, e.getStatus(), e.getMessage());
		}
	}
	public OcsFuture<OcsResult> execute(OcsOptions options) throws OcsException {
		if (options == null) {
			options = OcsOptions.defaultOptions();
		}
		this.trans = options.getTranscoder();
		return rpc.asyncCall(this, options.getTranscoder(), key, options.getTimeout());
	}

	public String getKey() {
		return this.key;
	}

	public OcsLazyDecoder getLazyDecoder() {
		return lazyDecoder;
	}

	public Map<String, BinaryMemcachedMessage> buildMessages() {
		return null;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Command: {");
		sb.append(" Opcode: " + this.opcode);
		sb.append(", Key: " + key);
		sb.append(", Trans: " + this.trans.name());
		sb.append(" }");
		return sb.toString();
	}
}
