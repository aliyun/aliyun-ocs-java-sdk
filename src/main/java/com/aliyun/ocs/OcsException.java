package com.aliyun.ocs;

public class OcsException extends Exception {
	private static final long serialVersionUID = 1L;
	private short status = -1;

	public OcsException(String message) {
		super(message);
	}

	public OcsException(String message, Throwable cause) {
		super(message, cause);
	}

	public OcsException(Throwable cause) {
		super(cause);
	}

	public short getStatus() {
		return this.status;
	}

}
