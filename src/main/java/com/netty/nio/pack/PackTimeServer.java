package com.netty.nio.pack;

import java.util.Date;

import com.nio.common.Commom;

import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 存在"黏包"的time server
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午5:00:59
 */
public class PackTimeServer {
    
    private static int badRequestCnt = 0;
    
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf readBuf = (ByteBuf)msg;
                                byte[] requestByte = new byte[readBuf.readableBytes()];
                                readBuf.readBytes(requestByte);
                                String request = new String(requestByte);
                                System.out.println("Get order : " + request);
                                if(Commom.COMMAND.equals(request)) {
                                    String response = new Date().toString();
                                    ByteBuf writeBuf = Unpooled.copiedBuffer(response.getBytes());
                                    ctx.write(writeBuf);
                                } else {
                                    //bad request
                                    //由于黏包会出现错误command
                                    System.out.println("Bad order count : " + ++badRequestCnt);
                                }
                            }
                            
                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                ctx.flush();
                            }
                            
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                ctx.close();
                            }
                        });
                    }
                });
        
            ChannelFuture channelFuturn = bootstrap.bind(Commom.PORT).sync();
            System.out.println("Pack time server bind at port : " + Commom.PORT);
            channelFuturn.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}


