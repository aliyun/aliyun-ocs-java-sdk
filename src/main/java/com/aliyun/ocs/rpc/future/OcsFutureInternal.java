package com.aliyun.ocs.rpc.future;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.protocol.memcached.binary.BinaryMemcachedMessage;
import com.aliyun.ocs.protocol.memcached.binary.lazydecoder.OcsLazyDecoder;
import com.aliyun.ocs.rpc.OcsReplyMessageWrapper;

public class OcsFutureInternal implements java.util.concurrent.Future<BinaryMemcachedMessage> {
	protected ReentrantLock lock = new ReentrantLock();
	protected Condition cond = lock.newCondition();
	protected OcsReplyMessageWrapper wrapper = null;
	protected Throwable exception = null;
	protected SocketAddress addr = null;
	protected ChannelFuture connectFuture;
	protected OcsLazyDecoder lazyDecoder;
	protected Object key = null;
	private Collection<Integer> opaques = null;
	private int waitCount = 0;
	private boolean noreply = false;
	private int dummyStatus;
	private boolean accessWrite = true;

	OcsFutureListener listener = null;

	public void setRemoteAddress(SocketAddress addr) {
		this.addr = addr;
	}

	public SocketAddress getRemoteAddress() {
		return this.addr;
	}

	public void setCtx(Object key) {
		this.key = key;
	}

	public Object getCtx() {
		return this.key;
	}

	public void setConnectFuture(ChannelFuture connectFuture) {
		this.connectFuture = connectFuture;
		this.connectFuture.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				innerNotifyAll();
			}
		});
	}

	public void setValue(OcsReplyMessageWrapper messageWrapper) throws OcsException {
		this.wrapper = messageWrapper;
		innerNotifyAll();
	}

	public boolean setException(Throwable r) {
		this.exception = r;
		innerNotifyAll();
		return false;
	}

	public void setListener(OcsFutureListener listener) {
		OcsFutureListener nowCall = null;
		try {
			lock.lock();
			if (isDone()) {
				nowCall = listener;
			} else {
				this.listener = listener;
			}
		} finally {
			lock.unlock();
		}
		if (nowCall != null) {
			nowCall.handle(this);
		}
	}

	private void innerNotifyAll() {
		OcsFutureListener nowCall = null;
		try {
			lock.lock();
			if (waitCount <= 1) {
				cond.signal();
			} else if (waitCount > 1) {
				cond.signalAll();
			}
			if (this.listener != null) {
				nowCall = this.listener;
				this.listener = null;
			}
		} finally {
			lock.unlock();
		}
		if (nowCall != null) {
			nowCall.handle(this);
		}
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	public boolean isDone() {
		boolean connectDone = false;
		if (connectFuture != null)
			connectDone = connectFuture.isDone();

		return connectDone || (wrapper != null || exception != null);
	}

	private BinaryMemcachedMessage innerGet() throws ExecutionException {
		if (exception != null) {
			throw new ExecutionException("remote: " + this.addr, exception);
		}

		if (wrapper == null)
			throw new ExecutionException(new NullPointerException("OcsMessage shouldn't return null"));
		try {
			wrapper.lazyDecode(this.lazyDecoder);
			return wrapper.getMessage();
		} catch (OcsException e) {
			// BUG, unreachable
			e.printStackTrace();
		}

		if (connectFuture != null) {
			if (connectFuture.getCause() != null)
				throw new ExecutionException("remote: " + this.addr, connectFuture.getCause());
			throw new ExecutionException(new Exception("connect future cause not null remote: " + this.addr));
		}
		throw new ExecutionException(new Exception("no result had been set, remote: " + this.addr));
	}

	private void innerWait(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			lock.lock();
			waitCount++;
			if (!isDone()) {
				if (timeout == -1)
					cond.await();
				else
					cond.await(timeout, unit);
			}
		} finally {
			waitCount--;
			lock.unlock();
		}
	}

	public BinaryMemcachedMessage get() throws InterruptedException, ExecutionException {
		if (!noreply && !isDone()) {
			innerWait(-1, null);
		}
		return innerGet();
	}

	public BinaryMemcachedMessage get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!isDone()) {
			innerWait(timeout, unit);
		}
		if (!isDone()) {
			throw new TimeoutException("Timeout, remote: " + this.addr);
		}
		return innerGet();
	}

	public void setLazyDecoder(OcsLazyDecoder lazyDecoder) {
		this.lazyDecoder = lazyDecoder;
	}

	public OcsLazyDecoder getLazyDecoder() {
		return this.lazyDecoder;
	}

	public int getDummyStatus() {
		return dummyStatus;
	}

	public void setDummyStatus(int dummyStatus) {
		this.dummyStatus = dummyStatus;
	}

	public Collection<Integer> getOpaques() {
		return opaques;
	}

	public void setOpaques(Collection<Integer> opaques) {
		this.opaques = opaques;
	}

	public boolean isAccessWrite() {
		return accessWrite;
	}

	public void setAccessWrite(boolean accessWrite) {
		this.accessWrite = accessWrite;
	}
}
