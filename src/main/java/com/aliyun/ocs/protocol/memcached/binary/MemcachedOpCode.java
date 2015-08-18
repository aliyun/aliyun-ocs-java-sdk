package com.aliyun.ocs.protocol.memcached.binary;

public class MemcachedOpCode {
	public static final byte GET = 0x00;
	public static final byte SET = 0x01;
	public static final byte ADD = 0x02;
	public static final byte REPLACE = 0x03;
	public static final byte DELETE = 0x04;
	public static final byte INCREMENT = 0x05;
	public static final byte DECREMENT = 0x06;
	public static final byte QUIT = 0x07;
	public static final byte FLUSH = 0x08;
	public static final byte GETQ = 0x09;
	public static final byte NOOP = 0x0a;
	public static final byte VERSION = 0x0b;
	public static final byte GETK = 0x0c;
	public static final byte GETKQ = 0x0d;
	public static final byte APPEND = 0x0e;
	public static final byte PREPEND = 0x0f;
	public static final byte STAT = 0x10;
	public static final byte SETQ = 0x11;
	public static final byte ADDQ = 0x12;
	public static final byte REPLACEQ = 0x13;
	public static final byte DELETEQ = 0x14;
	public static final byte INCREMENTQ = 0x15;
	public static final byte DECREMENTQ = 0x16;
	public static final byte QUITQ = 0x17;
	public static final byte FLUSHQ = 0x18;
	public static final byte APPENDQ = 0x19;
	public static final byte PREPENDQ = 0x1a;
	public static final byte TOUCH = 0x1c;
	public static final byte GAT = 0x1d;
	public static final byte GATQ = 0x1e;
	public static final byte GATK = 0x23;
	public static final byte GATKQ = 0x24;
	public static final byte SASL_LIST_MECHS = 0x20;
	public static final byte SASL_AUTH = 0x21;
	public static final byte SASL_STEP = 0x22;
}
