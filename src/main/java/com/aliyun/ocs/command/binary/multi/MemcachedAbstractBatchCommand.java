package com.aliyun.ocs.command.binary.multi;

import java.util.Map;
import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsOptions;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;
import com.aliyun.ocs.rpc.OcsRpc;

public abstract class MemcachedAbstractBatchCommand implements MemcachedBatchCommand {
	protected final OcsRpc rpc;
	protected byte opcode;
	protected OcsLazyDecoder lazyDecoder;
	protected OcsTranscoder trans;

	public MemcachedAbstractBatchCommand(final OcsRpc rpc, byte opcode) {
		this.rpc = rpc;
		this.opcode = opcode;
	}

	public BinaryMemcachedMessage buildMessage() {
		return null;
	}

	public OcsFuture<Map<String, OcsResult>> executeBatch(boolean write, OcsOptions options) throws OcsException {
		this.trans = options.getTranscoder();
		return rpc.asyncCall(this, trans, write, dummyStatus(), options.getBatchTimeout());
	}

	public Map<String, OcsResult> syncExecuteBatch(boolean write, OcsOptions options) throws OcsException {
		this.trans = options.getTranscoder();

		OcsFuture<Map<String, OcsResult>> future = executeBatch(write, options);
		try {
			Map<String, OcsResult> results = future.get();
			return results;
		} catch (Exception e) {
			throw new OcsException(e);
		}
	}

	public OcsLazyDecoder getLazyDecoder() {
		return lazyDecoder;
	}
}
