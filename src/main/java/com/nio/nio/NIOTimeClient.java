package com.nio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.nio.common.Commom;

/**
 * NIO的client端
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月13日 下午2:27:36
 */
public class NIOTimeClient {
    
    private static volatile boolean isStop = false;
    
    public static void main(String[] args) {
        SocketChannel socketChannel = null; 
        Selector selector = null;
        
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            
            if(socketChannel.connect(new InetSocketAddress("127.0.0.1", Commom.PORT))) {
                socketChannel.register(selector, SelectionKey.OP_WRITE);
            } else {
                //如果connect返回的是false,并不代表连接失败,只是服务端没有返回TCP握手应答信息
                //这时我们把channel注册到selector上面,注册OP_CONNECT操作位
                //当服务器返回TCP的syn-ack消息后,selector就会轮询到这个channel处理连接就绪状态
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
            
            while(!isStop) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iters = selectionKeys.iterator();
                while(iters.hasNext()) {
                    SelectionKey key = iters.next();
                    iters.remove();
                    try {
                        handleKey(key, selector);
                    } catch (Exception e) {
                        if(null!=key) {
                            key.cancel();
                            if(null!=key.channel()) {
                                key.channel().close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if(null!=selector) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void handleKey(SelectionKey key, Selector selector) throws IOException {
        if(key.isValid()) {
            SocketChannel socketChannel = (SocketChannel)key.channel();
            
            if(key.isConnectable()) { //如果是处于连接状态,说明服务端已经返回握手应答
                if(socketChannel.finishConnect()) {
                    //连接成功
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                } else {
                    //连接失败
                    System.out.println("Connect failed...");
                    System.exit(-1);
                }
            }
            
            if(key.isWritable()) {
                byte[] body = Commom.COMMAND.getBytes();
                ByteBuffer writerBuffer = ByteBuffer.allocate(body.length);
                writerBuffer.put(body);
                writerBuffer.flip();
                socketChannel.write(writerBuffer);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            
            if(key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(readBuffer);
                if(readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBytes];
                    readBuffer.get(bytes);
                    String body = new String(bytes);
                    System.out.println("response : " + body);
                    stop();
                }
            }
        }
    }
    
    private static void stop() {
        isStop = true;
    }
}
