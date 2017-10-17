package com.nio.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

import com.nio.common.Commom;

/**
 * 
 * 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 上午10:29:09
 */
public class AIOTimeClient {

    public static void main(String[] args) {
        AsyncTimeClientHandler asyncTimeClientHandler = new AsyncTimeClientHandler("127.0.0.1", Commom.PORT);
        Thread t1 = new Thread(asyncTimeClientHandler);
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {
    
    private AsynchronousSocketChannel client;
    
    private String host;
    private int port;
    
    private CountDownLatch latch;
    
    public AsyncTimeClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        
        try {
            this.client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        latch = new CountDownLatch(1);
        this.client.connect(new InetSocketAddress(this.host, this.port), this, this);
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void completed(Void result, AsyncTimeClientHandler attachment) {
        byte[] requestByte = Commom.COMMAND.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(requestByte.length);
        writeBuffer.put(requestByte);
        writeBuffer.flip();
        
        this.client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if(attachment.hasRemaining()) { 
                    //如果还有剩余字节,那么继续发送
                    client.write(attachment, attachment, this);
                } else {
                    //全部发送完成,就进行异步读取
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            byte[] responseByte = new byte[attachment.remaining()];
                            attachment.get(responseByte);
                            String body = new String(responseByte);
                            System.out.println("Now is : " + body);
                            latch.countDown();
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
        exc.printStackTrace();
        
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.latch.countDown();
    }
}
