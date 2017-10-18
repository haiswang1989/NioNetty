//package com.netty.nio.serialize.protobuf;
//
//import java.io.UnsupportedEncodingException;
//import java.util.concurrent.TimeUnit;
//
//import com.google.common.base.Stopwatch;
//import com.google.protobuf.ByteString;
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.serialize.bean.ProtoBufferSerializeBean;
//
///**
// * protobuf的序列化与反序列化
// * <p>Description:</p>
// * @author hansen.wang
// * @date 2017年3月2日 上午10:42:35
// */
//public class ProtobufSerialize {
//	
//	/**
//	 * 54ms (28bit)
//	 * 28ms 
//	 * @param args
//	 * @throws UnsupportedEncodingException
//	 */
//	public static void main(String[] args) throws UnsupportedEncodingException {
//		ProtoBufferSerializeBean.msgInfo.Builder builder = ProtoBufferSerializeBean.msgInfo.newBuilder();
//		
//		builder.setBoolVal(true);
//		builder.setByteVal(ByteString.copyFrom("1", "UTF-8"));
//		builder.setCharVal(ByteString.copyFrom("1", "UTF-8"));
//		builder.setShortVal(1);
//		builder.setIntVal(1);
//		builder.setLongVal(1);
//		builder.setFloatVal(1);
//		builder.setDoubleVal(1);
//		
//		int testCount = 100000;
//		
//		/***********序列化**********/
//		byte[] bytes = null; 
//		Stopwatch stopWatch = Stopwatch.createStarted();
//		for(int i=0; i<testCount; i++) {
//			bytes = builder.build().toByteArray();
//		}
//		long use = stopWatch.elapsed(TimeUnit.MILLISECONDS);
//		System.out.println("serialize use : " + use);
//		System.out.println("byte size : " + bytes.length);
//		
//		/***********反序列化**********/
//		
//		try {
//			stopWatch = Stopwatch.createStarted();
//			for(int i=0; i<testCount; i++) {
//				ProtoBufferSerializeBean.msgInfo.parseFrom(bytes);
//			}
//			use = stopWatch.elapsed(TimeUnit.MILLISECONDS);
//			System.out.println("deserialize use : " + use);
//		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
//		}
//	}
//
//}
