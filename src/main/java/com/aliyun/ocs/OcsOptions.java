package com.aliyun.ocs;

import com.aliyun.ocs.util.OcsDefaultTranscoder;

public class OcsOptions {
	private OcsTranscoder trans;
	private int timeout = 500;
	private int batchTimeout = 2 * timeout;

	public OcsOptions() {
		trans = new OcsDefaultTranscoder();
	}

	public OcsOptions(int timeout) {
		this.timeout = timeout;
		trans = new OcsDefaultTranscoder();
		batchTimeout = 2 * timeout;
	}
	public OcsOptions(int timeout, int batchTimeout) {
		this.timeout = timeout;
		trans = new OcsDefaultTranscoder();
		this.batchTimeout = batchTimeout;
	}
	public OcsOptions(int timeout, OcsTranscoder trans) {
		this.timeout = timeout;
		this.trans   = trans;
		batchTimeout = 2 * timeout;
	}
	public OcsOptions(int timeout, int batchTimeout, OcsTranscoder trans) {
		this.timeout = timeout;
		this.trans   = trans;
		this.batchTimeout = batchTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public static OcsOptions defaultOptions() {
		return new OcsOptions();
	}

	public OcsTranscoder getTranscoder() {
		return trans;
	}

	public int getBatchTimeout() {
		return batchTimeout;
	}

	public void setBatchTimeout(int batchTimeout) {
		this.batchTimeout = batchTimeout;
	}
}
