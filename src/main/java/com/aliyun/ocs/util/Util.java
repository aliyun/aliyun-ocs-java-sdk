package com.aliyun.ocs.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Comparator;

public class Util {
	public static SocketAddress cast2SocketAddress(String addr) {
		String[] str = addr.split(":");
		if (str.length != 2)
			throw new IllegalArgumentException();
		return new InetSocketAddress(str[0], Integer.valueOf(str[1]));
	}

	public static SocketAddress cast2SocketAddress(long id) {
		StringBuffer host = new StringBuffer(30);

		host.append((id & 0xff)).append('.');
		host.append(((id >> 8) & 0xff)).append('.');
		host.append(((id >> 16) & 0xff)).append('.');
		host.append(((id >> 24) & 0xff));

		int port = (int) ((id >> 32) & 0xffff);

		return new InetSocketAddress(host.toString(), port);
	}

	public static int getSystemConfigrationInt(String name, int defalutValue) {
		int configurationValue = defalutValue;
		String stringValue = System.getProperty(name);
		if (stringValue != null) {
			try {
				configurationValue = Integer.parseInt(stringValue);
			} catch (NumberFormatException e) {
			}
		}
		return configurationValue;
	}
	static class BytesComparator implements Comparator<byte[]> {

		public int compare(byte[] left, byte[] right) {
			for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
	            int a = (left[i] & 0xff);
	            int b = (right[j] & 0xff);
	            if (a != b) {
	                return a - b;
	            }
	        }
	        return left.length - right.length;
		}	
		
	}
	
	public static BytesComparator BYTES_COMPARATOR = new BytesComparator();
}
