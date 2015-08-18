package com.aliyun.ocs.protocol.memcached.binary.lazydecoder;

public class LazyDecoderFactory {
	public static OcsLazyDecoder LAZY_DECODER_INTERGER_2BYTEARRAY = new OcsLazyDecoderInteger2ByteArray();
	public static OcsLazyDecoder LAZY_DECODER_NONE_NONE = new OcsLazyDeocoderNoneNone();
	public static OcsLazyDecoder LAZY_DECODER_INTERGER_BYTEARRAY = new OcsLazyDecoderIntegerByteArray();
	public static OcsLazyDecoder LAZY_DECODER_NONE_BYTEARRAY = new OcsLazyDecoderNoneByteArray();
	public static OcsLazyDecoder LAZY_DECODER_NONE_LONG = new OcsLazyDecoderNoneLong();
}
