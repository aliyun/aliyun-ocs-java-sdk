package com.aliyun.ocs;

import com.aliyun.ocs.util.OcsBuffer;

public interface OcsTranscoder {
	public byte[] encodeKey(String key) throws OcsException;

	public String decodeKey(byte[] bytes) throws OcsException;

	public OcsBuffer encodeTo(Object Object) throws OcsException;

	public Object decodeFrom(OcsBuffer buffer) throws OcsException;

	public String name();
}
