package com.netty.nio.serialize.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.nio.common.SerializeBean;

/**
 * Java原生态序列化与反序列化
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年3月2日 上午10:41:50
 */
public class JavaSerialize {
	
	/**
	 * 4725ms(500161bit) 序列化
	 * 687ms 反序列化
	 * @param args
	 */
	public static void main(String[] args) {
		
		int testCount = 100000;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		
		SerializeBean bean = new SerializeBean();
		/***********序列化**********/
		Stopwatch stopwatch = null;
		byte[] bytes = null;
		try {
			oos = new ObjectOutputStream(baos);
			stopwatch = Stopwatch.createStarted();
			for(int i = 0; i<testCount; i++) {
				oos.writeObject(bean);
				bytes = baos.toByteArray();
			}
			long use = stopwatch.elapsed(TimeUnit.MILLISECONDS);
			System.out.println("serialize use : " + use);
			System.out.println("byte size : " + bytes.length);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null!=stopwatch && stopwatch.isRunning()) {
				stopwatch.stop();
			}
		}
		
		/***********反序列化**********/
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try {
			stopwatch = Stopwatch.createStarted();
			for(int i=0; i<testCount; i++) {
				bais.mark(bytes.length);
				ois = new ObjectInputStream(bais);
				bean = (SerializeBean)ois.readObject();
				bais.reset();
			}
			long use = stopwatch.elapsed(TimeUnit.MILLISECONDS);
			System.out.println("deserialize use : " + use);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
