package com.netty.nio.pack;

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

public class PackTimeClient {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelHandlerAdapter() {
                        
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //重复发生100次,这样会出现黏包
                            ByteBuf writeBuf = null;
                            for(int i=0; i<100; i++) {
                                writeBuf = Unpooled.copiedBuffer(Commom.COMMAND.getBytes());
                                ctx.writeAndFlush(writeBuf);
                            }
                        }
                        
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf readBuf = (ByteBuf)msg;
                            byte[] responseByte = new byte[readBuf.readableBytes()];
                            readBuf.readBytes(responseByte);
                            String response = new String(responseByte);
                            System.out.println("Now is : " + response);
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
        
        try {
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", Commom.PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
