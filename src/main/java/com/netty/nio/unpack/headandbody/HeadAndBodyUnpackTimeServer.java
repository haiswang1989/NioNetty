package com.netty.nio.unpack.headandbody;

import java.util.Date;
import java.util.List;

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
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 基于"回车换行"的解码器
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午5:00:59
 */
public class HeadAndBodyUnpackTimeServer {
    
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new UnpackTimeServerChannelInit());
            
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

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月18日 上午9:57:06
 */
class UnpackTimeServerChannelInit extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HeadAndBodyDecode());
        ch.pipeline().addLast(new StringDecoder());
        ch.pipeline().addLast(new UnpackTimeServerHandler());
    }
}

class HeadAndBodyDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int canReadBytes = in.readableBytes();
        while(canReadBytes >= 4) {
            in.markReaderIndex(); //这边先mark一下readindex,字节不够的时候,可以再次回到这
            int bodyLength = in.readInt();
            int needLength = 4 + bodyLength;
            if(canReadBytes < needLength) { //字节不够了重置readIndex,等待下一次read
                in.resetReaderIndex();
                return;
            }
            
            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            ByteBuf writeBuf = Unpooled.copiedBuffer(body);
            out.add(writeBuf);
            
            canReadBytes = in.readableBytes();
        }
        
        System.out.println("Read over...");
    }
    
}

/**
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月18日 上午9:56:59
 */
class UnpackTimeServerHandler extends ChannelHandlerAdapter {
    
    private int requestCnt = 0;
    
    private int badRequestCnt = 0;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //由于使用了StringDecoder,这边就可以直接转换成String了
        String request = (String)msg;
        System.out.println("Receive order : " + request + ", " + ++requestCnt);
        if(Commom.COMMAND.equals(request)) {
            String response = new Date().toString();
            response += System.getProperty("line.separator");
            ByteBuf writeBuf = Unpooled.copiedBuffer(response.getBytes());
            ctx.writeAndFlush(writeBuf);
        } else {
            //bad request
            //由于黏包会出现错误command
            System.out.println("Bad order count : " + ++badRequestCnt);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}


