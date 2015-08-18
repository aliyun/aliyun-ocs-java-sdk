package com.aliyun.ocs;

public class OcsReplyStatus {
	public static  int REPLY_SUCCESS = 0x00;
	public static  int REPLY_ERROR_KEY_NOT_FOUND = 0x01;
	public static  int REPLY_ERROR_KEY_EXISTS = 0x02;
	public static  int REPLY_ERROR_TOO_BIG = 0x03;
	public static  int REPLY_ERROR_INVALD_ARG = 0x04;
	public static  int REPLY_ERROR_NOT_STORED = 0x05;
	public static  int REPLY_ERROR_DELTA_BADVAL = 0x06;
	public static  int REPLY_ERROR_NOT_MY_VBUCKET = 0x07;
	public static  int REPLY_AUTH_ERROR = 0x20;
	public static  int REPLY_AUTH_CONTINUE = 0x21;
	public static  int REPLY_ERROR_UNKNOWN_COMMAND = 0x81;
	public static  int REPLY_ERROR_NOMEM = 0x82;
	public static  int REPLY_ERROR_NOT_SUPPORTED = 0x83;
	public static  int REPLY_ERROR_INTERNAL = 0x84;
	public static  int REPLY_ERROR_BUSY = 0x85;
	public static  int REPLY_ERROR_TMPFAIL = 0x86;
	

}
