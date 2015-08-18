package com.aliyun.ocs;

import java.util.concurrent.Future;

import com.aliyun.ocs.rpc.future.OcsFutureListener;

public abstract class OcsFuture<T> implements Future<T> {
	Object ctx;

	public void setContext(Object c) {
		this.ctx = c;
	}

	public Object getContext() {
		return ctx;
	}

	public void setListener(OcsFutureListener futureListener) {
	}
}
