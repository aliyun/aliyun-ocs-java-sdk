package com.aliyun.ocs;

import java.util.HashMap;
import java.util.Map;

public class OcsResult {
	private static Map<Integer, String> statusMessage = new HashMap<Integer, String>();
	static {
		statusMessage.put(OcsReplyStatus.REPLY_SUCCESS, "success");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_KEY_NOT_FOUND, "key not found");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_KEY_EXISTS, "key exist");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_TOO_BIG, "vale too large");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_INVALD_ARG, "invalid arguments");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_NOT_STORED, "not stored");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_DELTA_BADVAL, "delta bad value");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_NOT_MY_VBUCKET, "not own bucket");
		statusMessage.put(OcsReplyStatus.REPLY_AUTH_ERROR, "auth reject");
		statusMessage.put(OcsReplyStatus.REPLY_AUTH_CONTINUE, "auth continue");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_UNKNOWN_COMMAND, "unknown command");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_NOMEM, "no mem");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_NOT_SUPPORTED, "not supported");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_INTERNAL, "internal error");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_BUSY, "busy");
		statusMessage.put(OcsReplyStatus.REPLY_ERROR_TMPFAIL, "tmp fail");
	}
	private Object result = null;
	private String key = null;
	private int status = -1;
	private String error = null;
	private long cas;

	public OcsResult(int status) {
		this.status = status;
	}

	public OcsResult(Object object, String key, long cas, int status) {
		this(object, status);
		this.key = key;
		this.cas = cas;
	}

	public OcsResult(Object result, int status) {
		this.result = result;
		this.status = status;
		this.cas = 0;
	}

	public OcsResult(Object object, String key, int status) {
		this(object, status);
		this.key = key;
		this.cas = 0;
	}

	public OcsResult(Object object, String key, int status, String error) {
		this(object, status);
		this.key = key;
		this.cas = 0;
		this.error = error;
	}

	public Object getValue() {
		return result;
	}

	public String getKey() {
		return this.key;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{status:" + status + " message:" + statusMessage.get(status));
		if (result != null) {
			sb.append(", result: ");
			if (result instanceof byte[]) {
				String str = new String((byte[]) result);
				sb.append(str);
			} else if (result instanceof Long) {
				sb.append(String.valueOf(result));
			} else {
				sb.append(result);
			}
		}

		sb.append(", cas: ");
		sb.append(cas);

		if (key != null) {
			sb.append(", key: ");
			sb.append(key);
		}
		sb.append(" }");
		return sb.toString();
	}

	public long getCas() {
		return cas;
	}

	public String getError() {
		return error;
	}

	public boolean isSuccess() {
		return this.status == OcsReplyStatus.REPLY_SUCCESS;
	}
}
