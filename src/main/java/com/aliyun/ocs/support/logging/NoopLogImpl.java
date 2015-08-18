package com.aliyun.ocs.support.logging;

public class NoopLogImpl implements Log {
	public void error(String msg, Throwable e) {
		System.out.println(msg + ", exception: " + e);
	}

	public void error(String msg) {
		System.out.println(msg);
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public void info(String msg) {
		System.out.println(msg);
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public void debug(String msg) {
		System.out.println(msg);
	}

	public void debug(String msg, Throwable e) {
		System.out.println(msg + ", exception: " + e);
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void warn(String msg) {
		System.out.println(msg);
	}

	public void warn(String msg, Throwable e) {
		System.out.println(msg + ", exception: " + e);
	}
}
