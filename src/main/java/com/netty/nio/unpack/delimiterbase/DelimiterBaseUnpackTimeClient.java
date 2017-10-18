package com.netty.nio.unpack.delimiterbase;

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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 
 * 基于"分隔符"的解码器
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午6:31:33
 */
public class DelimiterBaseUnpackTimeClient {
    
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new UnpackTimeClientChannelInit());
            
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", Commom.PORT).sync();
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
 * @date 2017年10月18日 上午9:59:30
 */
class UnpackTimeClientChannelInit extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ByteBuf delimiter = Unpooled.copiedBuffer(Commom.DELIMITER.getBytes());
        //这边可以传入多个分隔符
        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter, delimiter));
        ch.pipeline().addLast(new StringDecoder());
        ch.pipeline().addLast(new UnpackTimeClientHandler());
    }
}

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月18日 上午9:59:27
 */
class UnpackTimeClientHandler extends ChannelHandlerAdapter {
    
    private int count = 0;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        //发送指令的时候需要加上分隔符
        String command = Commom.COMMAND + Commom.DELIMITER;
        byte[] commandByte = command.getBytes();
        for(int i=0; i<100; i++) {
            message = Unpooled.buffer(commandByte.length);
            message.writeBytes(commandByte);
            ctx.writeAndFlush(message);
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String response = (String)msg;
        System.out.println("Now is : " + response + " ,count : " + ++count);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
