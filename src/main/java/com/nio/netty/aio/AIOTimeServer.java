package com.nio.netty.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import com.nio.netty.common.Commom;

import lombok.Getter;

/**
 * AIO 时间服务器
 * 
 * 
 * CompletionHandler<V, A>
 * V：表示I/O操作的返回值
 * A：表示绑定到I/O操作上的类型
 * 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月16日 下午6:00:16
 */
public class AIOTimeServer {

    public static void main(String[] args) {
        AsyncTimeSerevrHandler asyncTimeSerevrHandler = new AsyncTimeSerevrHandler();
        Thread targetThread = new Thread(asyncTimeSerevrHandler);
        targetThread.start();
        try {
            targetThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class AsyncTimeSerevrHandler implements Runnable {
    
    @Getter
    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;
    
    @Getter
    private CountDownLatch latch;
    
    public AsyncTimeSerevrHandler() {
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(Commom.PORT));
            System.out.println("Time server start in port : " + Commom.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        //这边的CountDownLatch就是为了让接收线程堵塞,防止执行完成直接退出
        //在实际的项目中无需这边单独的启动一个线程
        latch = new CountDownLatch(1);
        asynchronousServerSocketChannel.accept(this, new AccepteCompletionHandler());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * accept处理类 
 * 
 * 处理accept一般为
 * CompletionHandler<AsynchronousSocketChannel, ? super A>类型的handler
 * 
 * AsynchronousSocketChannel(返回值),就是server端与client端的连接对象
 * ? super A(绑定在I/O操作上的对象),可以理解为从"AsyncTimeSerevrHandler"给"AccepteCompletionHandler"传入的参数
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月16日 下午6:29:45
 */
class AccepteCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeSerevrHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeSerevrHandler attachment) {
        //这边需要继续的调用AsyncTimeSerevrHandler中的AsynchronousServerSocketChannel
        //去accept其他新的连接,这样形成一个循环,每个接收的客户端连接成功后,再异步接收新的客户端连接
        attachment.getAsynchronousServerSocketChannel().accept(attachment, this);
        
        //这边是异步读取数据
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        
       /* 
        * read(ByteBuffer dst, A attachment, CompletionHandler<Integer,? super A> handler) 
        * dst：接收的缓冲区,用于异步的从channel中读取数据包
        * attachment：绑定在I/O操作上的对象,通知回调的时候作为入参使用,可以为null
        * handler：接收回调的handler
        */
        result.read(readBuffer, readBuffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeSerevrHandler attachment) {
        exc.printStackTrace();
        attachment.getLatch().countDown();
    }
}

/**
 * read处理类
 * 
 * read(ByteBuffer dst, A attachment, CompletionHandler<Integer,? super A> handler) 
 * dst：接收的缓冲区,用于异步的从channel中读取数据包
 * attachment：绑定在I/O操作上的对象,通知回调的时候作为入参使用,可以为null
 * handler：接收回调的handler
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 上午9:08:04
 */
class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    
    private AsynchronousSocketChannel channel;
    
    public ReadCompletionHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }
    
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        
        attachment.get(body);
        String request = new String(body);
        System.out.println("The time server recevie order : " + request);
        String response = Commom.COMMAND.equals(request) ? new Date().toString() : "Bad order";
        doWrite(response);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        exc.printStackTrace();
        
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void doWrite(String response) {
        byte[] responseByte = response.getBytes();
        ByteBuffer sendBuffer = ByteBuffer.allocate(responseByte.length);
        sendBuffer.put(responseByte);
        sendBuffer.flip();
        
        this.channel.write(sendBuffer, sendBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                //如果还有剩余字节没有发生完成,需要继续发送,直到全部发送成功
                if(attachment.hasRemaining()) {
                    channel.write(attachment, attachment, this);
                }
            }
            
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
