package com.nio.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.nio.netty.common.Commom;

/**
 * NIO的time server
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月12日 下午7:01:35
 */
public class NIOTimeServer {
    
    private static volatile boolean isStop = false;
    
    public static void main(String[] args) {
        ServerSocketChannel serverSocketChannel = null;
        Selector selector = null;
        
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            //非堵塞式
            serverSocketChannel.configureBlocking(false);
            //监听端口
            //backlog是指tcp/ip协议中保持连接(全连接和半连接)的队列长度
            //关于backlog的详细分析：http://www.cnxct.com/something-about-phpfpm-s-backlog/
            serverSocketChannel.socket().bind(new InetSocketAddress(Commom.PORT),1024);
            //将channel注册到selector上面
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Time server start...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            while(!isStop) {
              //在selector轮询"活跃"的channel
                selector.select();
                //准备就绪的channel
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iters = selectedKeys.iterator();
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
    
    /**
     * 处理活跃的channel
     * @param key
     * @throws IOException
     */
    private static void handleKey(SelectionKey key, Selector selector) throws IOException {
        if(key.isValid()) { //此键是否准备就绪
            if(key.isAcceptable()) { //此通道是否准备好接收新的套接字(接收连接)
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                //将连接注册到channel上,并且轮询检测READ事件
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            
            if(key.isReadable()) { //此通道是否准备好读取数据
                SocketChannel socketChannel = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //由于上面设置了"异步非堵塞"模式,需要通过返回值来判定读取到的结果
                //如果返回值 大于 0,读到了字节,对字节进行编解码
                //如果返回值 等于 0,没有读到字节,属于正常场景,忽略
                //如果返回值 等于 -1,链路已经关闭,需要关闭socketchannel,是否资源
                int readBytes = socketChannel.read(readBuffer);
                if(readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes);
                    String response = null;
                    System.out.println("Recive order : " + body);
                    if(Commom.COMMAND.equals(body)) {
                        response = new Date().toString();
                        doWrite(socketChannel, response);
                    } else {
                        response = "ok";
                        doWrite(socketChannel, response);
                        stop();
                    }
                } else if(readBytes < 0) {
                    //对于该elsif分支何时能到达?
                    //TODO
                    System.out.println("Client closed.");
                    key.cancel();
                    socketChannel.close();
                } else { //readBytes==0
                    //正常情况
                }
            }
        }
    }
    
    /**
     * 客户端写
     * @param socketChannel
     * @param response
     * @throws IOException
     */
    private static void doWrite(SocketChannel socketChannel, String response) throws IOException {
        byte[] bytes = response.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
    }
    
    /**
     * 停止服务端
     */
    private static void stop() {
        isStop = true;
    }
}
