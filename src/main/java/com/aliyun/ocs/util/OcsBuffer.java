package com.aliyun.ocs.util;

public class OcsBuffer {
	
	public OcsBuffer(byte[] b, int f) {
		this.buffer = b;
		this.flag = f;
	}

	private byte[] buffer;
	private int flag;

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public int getLength() {
		return buffer.length;
	}

}
