package com.netty.nio.serialize.json;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.nio.common.SerializeBean;

/**
 * alibaba fastjson的序列化与反序列化
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年3月2日 上午10:42:14
 */
public class JsonSerialize {

	/**
	 * 序列化 230ms(114bit)
	 * 反序列化 192ms
	 * @param args
	 */
	public static void main(String[] args) {
		
		int testCount = 100000;
		
		SerializeBean bean = new SerializeBean();
		byte[] bytes = null;
		/***********序列化**********/
		Stopwatch stopWatch = Stopwatch.createStarted();
		for(int i=0; i<testCount; i++) {
			bytes = JSON.toJSONString(bean).getBytes();
		}
		long use = stopWatch.elapsed(TimeUnit.MILLISECONDS);
		System.out.println("serialize use : " + use);
		System.out.println("byte size : " + bytes.length);
		
		/***********反序列化**********/
		stopWatch = Stopwatch.createStarted();
		String jsonString = new String(bytes);
		for(int i=0; i<testCount; i++) {
			bean = JSON.parseObject(jsonString, SerializeBean.class);
		}
		use = stopWatch.elapsed(TimeUnit.MILLISECONDS);
		System.out.println("deserialize use : " + use);
	}

}
