package com.netty.nio.simple;

import com.nio.common.Commom;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyTimeClient {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class) //设置Channel为NioSocketChannel,对应于JDK中的SocketChannel
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel arg0) throws Exception {
                        arg0.pipeline().addLast(new TimeClientHandler());
                    }
                });
            
            //发起同步连接
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", Commom.PORT).sync();
            //堵塞main方法,直到客户端关闭链路
            //exceptionCaught()中关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午3:36:01
 */
class TimeClientHandler extends ChannelHandlerAdapter {
    
    /**
     * 当客户端和服务端TCP链路建立成功之后
     * Netty的NIO线程会调用channelActive方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf writeBuf = Unpooled.copiedBuffer(Commom.COMMAND.getBytes());
        ctx.writeAndFlush(writeBuf);
    }
    
    /**
     * 当服务端返回应答消息时,channelRead会被调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf readBuf = (ByteBuf)msg;
        byte[] responseByte = new byte[readBuf.readableBytes()];
        readBuf.readBytes(responseByte);
        String response = new String(responseByte);
        System.out.println("Now is : " + response);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
