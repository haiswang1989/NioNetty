package com.nio.common;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SerializeBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//1字节
	private byte byteVal = 0;
	//2字节
	private char charVal = 0;
	//2字节
	private short shortVal = 0;
	//4字节
	private int intVal = 0;
	//8字节
	private long longVal = 0;
	//4字节
	private float floatVal = 0;
	//8字节
	private double doubleVal = 0;
	//1位 1/8字节
	private boolean boolVal = true;
}
