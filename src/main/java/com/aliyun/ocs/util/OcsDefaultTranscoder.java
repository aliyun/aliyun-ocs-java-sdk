package com.aliyun.ocs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.aliyun.ocs.OcsException;
import com.aliyun.ocs.OcsTranscoder;
import com.aliyun.ocs.support.logging.Log;
import com.aliyun.ocs.support.logging.LogFactory;

public class OcsDefaultTranscoder implements OcsTranscoder {
	Log logger = LogFactory.getLog(OcsDefaultTranscoder.class);
	static String NAME = "DefaultTranscoder";
	// General flags
	static final int SERIALIZED = 1;
	static final int COMPRESSED = 2;

	// Special flags for specially handled types.
	private static final int SPECIAL_MASK = 0xff00;
	static final int SPECIAL_BOOLEAN = (1 << 8);
	static final int SPECIAL_INT = (2 << 8);
	static final int SPECIAL_LONG = (3 << 8);
	static final int SPECIAL_DATE = (4 << 8);
	static final int SPECIAL_BYTE = (5 << 8);
	static final int SPECIAL_FLOAT = (6 << 8);
	static final int SPECIAL_DOUBLE = (7 << 8);
	static final int SPECIAL_BYTEARRAY = (8 << 8);
	private static final String DEFAULT_CHARSET = "UTF-8";
	protected String charset = DEFAULT_CHARSET;
	private static boolean packZeros = true;

	public OcsDefaultTranscoder() {
	}

	public OcsBuffer encodeTo(Object o) throws OcsException {
		byte[] b = null;
		int flags = 0;
		try {
			if (o instanceof String) {
				b = encodeString((String) o);
			} else if (o instanceof Long) {
				b = encodeLong((Long) o);
				flags |= SPECIAL_LONG;
			} else if (o instanceof Integer) {
				b = encodeInt((Integer) o);
				flags |= SPECIAL_INT;
			} else if (o instanceof Boolean) {
				b = encodeBoolean((Boolean) o);
				flags |= SPECIAL_BOOLEAN;
			} else if (o instanceof Date) {
				b = encodeLong(((Date) o).getTime());
				flags |= SPECIAL_DATE;
			} else if (o instanceof Byte) {
				b = encodeByte((Byte) o);
				flags |= SPECIAL_BYTE;
			} else if (o instanceof Float) {
				b = encodeInt(Float.floatToRawIntBits((Float) o));
				flags |= SPECIAL_FLOAT;
			} else if (o instanceof Double) {
				b = encodeLong(Double.doubleToRawLongBits((Double) o));
				flags |= SPECIAL_DOUBLE;
			} else if (o instanceof byte[]) {
				b = (byte[]) o;
				flags |= SPECIAL_BYTEARRAY;
			} else {
				b = serialize(o);
				flags |= SERIALIZED;
			}
			assert b != null;
			return new OcsBuffer(b, flags);
		} catch (Exception e) {
			throw new OcsException(e);
		}
	}

	public Object decodeFrom(OcsBuffer d) throws OcsException {
		byte[] data = d.getBuffer();
		Object rv = null;
		int flags = d.getFlag() & SPECIAL_MASK;
		try {
			if ((d.getFlag() & SERIALIZED) != 0 && data != null) {
				rv = deserialize(data);
			} else if (flags != 0 && data != null) {
				switch (flags) {
				case SPECIAL_BOOLEAN:
					rv = Boolean.valueOf(decodeBoolean(data));
					break;
				case SPECIAL_INT:
					rv = Integer.valueOf(decodeInt(data));
					break;
				case SPECIAL_LONG:
					rv = Long.valueOf(decodeLong(data));
					break;
				case SPECIAL_DATE:
					rv = new Date(decodeLong(data));
					break;
				case SPECIAL_BYTE:
					rv = Byte.valueOf(decodeByte(data));
					break;
				case SPECIAL_FLOAT:
					rv = new Float(Float.intBitsToFloat(decodeInt(data)));
					break;
				case SPECIAL_DOUBLE:
					rv = new Double(Double.longBitsToDouble(decodeLong(data)));
					break;
				case SPECIAL_BYTEARRAY:
					rv = data;
					break;
				default:
					logger.error("Unknown flags: " + flags);
					throw new OcsException("Unknown flags: " + flags);
				}
			} else {
				rv = decodeString(data);
			}
			return rv;
		} catch (Exception e) {
			throw new OcsException(e);
		}
	}

	protected String decodeString(byte[] data) {
		String rv = null;
		try {
			if (data != null) {
				rv = new String(data, charset);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return rv;
	}

	protected byte[] encodeString(String in) {
		byte[] rv = null;
		try {
			rv = in.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return rv;
	}

	protected byte[] serialize(Object o) {
		if (o == null) {
			throw new NullPointerException("Can't serialize null");
		}
		byte[] rv = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;
		try {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			os.writeObject(o);
			os.close();
			bos.close();
			rv = bos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Non-serializable object", e);
		} finally {
			close(os);
			close(bos);
		}
		return rv;
	}

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				// logger.info("Unable to close %s", closeable, e);
			}
		}
	}

	protected Object deserialize(byte[] in) throws IOException, ClassNotFoundException {
		Object rv = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if (in != null) {
				bis = new ByteArrayInputStream(in);
				is = new ObjectInputStream(bis);
				rv = is.readObject();
				is.close();
				bis.close();
			}
		} catch (IOException e) {
			logger.error("IOException decoding: " + (in == null ? 0 : in.length) + " bytes of data: " + e);
			throw e;
		} catch (ClassNotFoundException e) {
			logger.error("Caught CNNFE decoding: " + (in == null ? 0 : in.length) + " bytes of data: " + e);
			throw e;
		} finally {
			close(is);
			close(bis);
		}
		return rv;
	}

	public static byte[] encodeNum(long l, int maxBytes) {
		byte[] rv = new byte[maxBytes];
		for (int i = 0; i < rv.length; i++) {
			int pos = rv.length - i - 1;
			rv[pos] = (byte) ((l >> (8 * i)) & 0xff);
		}
		if (packZeros) {
			int firstNon0 = 0;
			// Just looking for what we can reduce
			while (firstNon0 < rv.length && rv[firstNon0] == 0) {
				firstNon0++;
			}
			if (firstNon0 > 0) {
				byte[] tmp = new byte[rv.length - firstNon0];
				System.arraycopy(rv, firstNon0, tmp, 0, rv.length - firstNon0);
				rv = tmp;
			}
		}
		return rv;
	}

	public static byte[] encodeLong(long l) {
		return encodeNum(l, 8);
	}

	public static long decodeLong(byte[] b) {
		long rv = 0;
		for (byte i : b) {
			rv = (rv << 8) | (i < 0 ? 256 + i : i);
		}
		return rv;
	}

	public static byte[] encodeInt(int in) {
		return encodeNum(in, 4);
	}

	public static int decodeInt(byte[] in) {
		assert in.length <= 4 : "Too long to be an int (" + in.length + ") bytes";
		return (int) decodeLong(in);
	}

	public static byte[] encodeByte(byte in) {
		return new byte[] { in };
	}

	public static byte decodeByte(byte[] in) {
		assert in.length <= 1 : "Too long for a byte";
		byte rv = 0;
		if (in.length == 1) {
			rv = in[0];
		}
		return rv;
	}

	public static byte[] encodeBoolean(boolean b) {
		byte[] rv = new byte[1];
		rv[0] = (byte) (b ? '1' : '0');
		return rv;
	}

	public static boolean decodeBoolean(byte[] in) {
		assert in.length == 1 : "Wrong length for a boolean";
		return in[0] == '1';
	}

	public byte[] encodeKey(String key) throws OcsException {
		try {
			return key.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new OcsException(e);
		}
	}

	public String decodeKey(byte[] bytes) throws OcsException {
		try {
			return new String(bytes, charset);
		} catch (UnsupportedEncodingException e) {
			throw new OcsException(e);
		}
	}

	public String name() {
		return NAME;
	}
}
