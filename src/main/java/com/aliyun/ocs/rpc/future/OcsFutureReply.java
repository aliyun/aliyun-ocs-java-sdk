package com.aliyun.ocs.rpc.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;

public class OcsFutureReply extends OcsFuture<OcsResult> {

	OcsFutureInternal impl;
	String key;
	OcsTranscoder trans;

	public OcsFutureReply(final OcsFutureInternal impl, OcsTranscoder trans, String key) {
		this.impl = impl;
		this.key = key;
		this.trans = trans;
	}

	public void setListener(final OcsFutureListener listener) {
		impl.setListener(listener);
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return impl.cancel(mayInterruptIfRunning);
	}

	public boolean isCancelled() {
		return impl.isCancelled();
	}

	public boolean isDone() {
		return impl.isDone();
	}

	public OcsResult get() throws InterruptedException, ExecutionException {
		try {
			return innerGet();
		} catch (OcsException e) {
			throw new ExecutionException(e);
		}
	}

	public OcsResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		try {
			return innerGet(timeout, unit);
		} catch (OcsException e) {
			throw new ExecutionException(e);
		}
	}

	protected OcsResult innerGet() throws InterruptedException, ExecutionException, OcsException {
		BinaryMemcachedMessage message = impl.get();
		if (message == null)
			throw new ExecutionException(new NullPointerException("Reply packet shouldn't return null"));

		OcsLazyDecoder lazyDecoder = impl.getLazyDecoder();
		return lazyDecoder.lazyConstruct(message, key, trans);
	}

	protected OcsResult innerGet(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, OcsException {
		BinaryMemcachedMessage message = impl.get(timeout, unit);
		if (message == null)
			throw new ExecutionException(new NullPointerException("Reply packet shouldn't return null"));

		OcsLazyDecoder lazyDecoder = impl.getLazyDecoder();
		return lazyDecoder.lazyConstruct(message, key, trans);
	}
}