package com.nio.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AboutByteBuf {

    public static void main(String[] args) {
        String  request = "request";
        ByteBuf requestBuf = Unpooled.copiedBuffer(request.getBytes());
        
        int readIndex = requestBuf.readerIndex();
        int writeIndex = requestBuf.writerIndex();
        int capacity = requestBuf.capacity();
        System.out.println("read index : " + readIndex);
        System.out.println("write index : " + writeIndex);
        System.out.println("capacity : " + capacity);
    }
}
