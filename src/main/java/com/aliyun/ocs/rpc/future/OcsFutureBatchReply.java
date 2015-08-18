package com.aliyun.ocs.rpc.future;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsFuture;
import com.aliyun.ocs.OcsReplyStatus;
import com.aliyun.ocs.OcsResult;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;
import com.aliyun.ocs.rpc.OcsChannel;
import com.aliyun.ocs.rpc.OcsReplyMessageWrapper;

public class OcsFutureBatchReply extends OcsFuture<Map<String, OcsResult>> {
	private final Map<String, Integer> opaques;
	private final OcsFutureInternal noopReply;
	private final String noopKey;
	private final OcsTranscoder trans;
	protected OcsLazyDecoder lazyDecoder;
	protected OcsLazyDecoder noopLazyDecoder;
	private final OcsChannel channel;
	protected int dummyStatus;

	public OcsFutureBatchReply(OcsChannel channel, Map<String, Integer> opaques, OcsFutureInternal noopReply, String noopKey, OcsTranscoder trans,
			OcsLazyDecoder lazyDecoder, int dummyStatus) {
		this.channel = channel;
		this.noopReply = noopReply;
		this.opaques = opaques;
		this.noopKey = noopKey;
		this.trans = trans;
		this.lazyDecoder = lazyDecoder;
		this.dummyStatus = dummyStatus;
	}

	class OcsFutureListenerImpl implements OcsFutureListener {
		public void handle(OcsFutureInternal ocsFuture) {

		}
	}

	public void setListener(final OcsFutureListener listener) {
		noopReply.setListener(listener);
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return noopReply.cancel(mayInterruptIfRunning);
	}

	public boolean isCancelled() {
		return noopReply.isCancelled();
	}

	public boolean isDone() {
		return noopReply.isDone();
	}

	private BinaryMemcachedMessage lazyDeocde(OcsReplyMessageWrapper wrapper) throws OcsException {
		if (wrapper == null)
			throw new OcsException(new NullPointerException("OcsMessage shouldn't return null"));
		try {
			wrapper.lazyDecode(this.lazyDecoder);
			return wrapper.getMessage();
		} catch (OcsException e) {
			// BUG, unreachable
			throw e;
		}
	}

	public Map<String, OcsResult> get() throws InterruptedException, ExecutionException {
		BinaryMemcachedMessage lastMessage = noopReply.get();
		if (lastMessage == null)
			throw new ExecutionException(new NullPointerException("Reply packet shouldn't return null"));
		OcsLazyDecoder noopLazyDecoder = noopReply.getLazyDecoder();
		try {
			OcsResult result = noopLazyDecoder.lazyConstruct(lastMessage, noopKey, trans);

			Map<String, OcsResult> resultSet = new HashMap<String, OcsResult>();
			if (result.getStatus() == OcsReplyStatus.REPLY_SUCCESS) {

				for (Entry<String, Integer> entry : opaques.entrySet()) {
					int opaque = entry.getValue();
					if (noopReply.isAccessWrite()) {
						int x = channel.removeAndGetWriteHeap(opaque);
						if (x != -1) {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), x));
						} else {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), dummyStatus));
						}
					} else {
						// lazy decode style
						OcsReplyMessageWrapper wrapper = channel.removeAndGetReadHeap(opaque);
						if (wrapper != null) {
							BinaryMemcachedMessage m = lazyDeocde(wrapper);
							OcsResult r = lazyDecoder.lazyConstruct(m, entry.getKey(), trans);
							resultSet.put(entry.getKey(), r);
						} else {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), dummyStatus));
						}
					}

				}
			} else {
				for (Entry<String, Integer> entry : opaques.entrySet()) {
					resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), result.getStatus()));
				}
			}
			return resultSet;
		} catch (OcsException e) {
			throw new ExecutionException(e);
		}
	}

	public Map<String, OcsResult> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		BinaryMemcachedMessage lastMessage = noopReply.get(timeout, unit);
		if (lastMessage == null)
			throw new ExecutionException(new NullPointerException("Reply packet shouldn't return null"));
		OcsLazyDecoder noopLazyDecoder = noopReply.getLazyDecoder();
		try {
			OcsResult result = noopLazyDecoder.lazyConstruct(lastMessage, noopKey, trans);

			Map<String, OcsResult> resultSet = new HashMap<String, OcsResult>();
			if (result.getStatus() != OcsReplyStatus.REPLY_SUCCESS) {

				for (Entry<String, Integer> entry : opaques.entrySet()) {
					int opaque = entry.getValue();
					if (noopReply.isAccessWrite()) {
						int x = channel.removeAndGetWriteHeap(opaque);
						if (x != -1) {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), x));
						} else {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), dummyStatus));
						}
					} else {
						// lazy decode style
						OcsReplyMessageWrapper wrapper = channel.removeAndGetReadHeap(opaque);
						if (wrapper != null) {
							BinaryMemcachedMessage m = lazyDeocde(wrapper);
							OcsResult r = lazyDecoder.lazyConstruct(m, entry.getKey(), trans);
							resultSet.put(entry.getKey(), r);
						} else {
							resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), dummyStatus));
						}
					}

				}
			} else {
				for (Entry<String, Integer> entry : opaques.entrySet()) {
					resultSet.put(entry.getKey(), new OcsResult(null, entry.getKey(), result.getStatus()));
				}
			}
			return resultSet;
		} catch (OcsException e) {
			throw new ExecutionException(e);
		}
	}
}
