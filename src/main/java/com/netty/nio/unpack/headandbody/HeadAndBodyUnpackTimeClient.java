package com.netty.nio.unpack.headandbody;

import java.util.concurrent.TimeUnit;

import com.nio.common.Commom;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午6:31:33
 */
public class HeadAndBodyUnpackTimeClient {
    
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //nothing
                        System.out.println("Do nothing...");
                    }
                });
            
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", Commom.PORT).sync();
            Channel channel = channelFuture.channel();
            ByteBuf message = null;
            byte[] commandByte = Commom.COMMAND.getBytes();
            for(int i=0; i<1000; i++) {
                int bodyLength = commandByte.length;
                message = Unpooled.buffer(4 + bodyLength);
                message.writeInt(bodyLength);
                message.writeBytes(commandByte);
                channel.writeAndFlush(message);
            }
            
            TimeUnit.SECONDS.sleep(10000);
            
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}