package com.aliyun.ocs.rpc;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class OcsRpcID implements Delayed {

	private OcsChannel session;
	private Integer opaque;
	private long delayed;

	public OcsRpcID(OcsChannel session, Integer opaque, long timeout) {
		this.setSession(session);
		this.setOpaque(opaque);
		delayed = timeout + System.currentTimeMillis();
	}

	public int compareTo(Delayed o) {
		OcsRpcID obj = (OcsRpcID) o;
		long r = this.delayed - obj.delayed;
		if (r < 0)
			return -1;
		else if (r == 0)
			return 0;
		else
			return 1;
	}

	public long getDelay(TimeUnit unit) {
		return unit.convert(delayed - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public OcsChannel getSession() {
		return session;
	}

	public void setSession(OcsChannel session) {
		this.session = session;
	}

	public Integer getOpaque() {
		return opaque;
	}

	public void setOpaque(Integer opaque) {
		this.opaque = opaque;
	}

}