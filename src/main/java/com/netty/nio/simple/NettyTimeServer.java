package com.netty.nio.simple;

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
 * Netty办的Time Server服务端
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午3:51:06
 */
public class NettyTimeServer {

    public static void main(String[] args) {
        //创建两个"reactor"线程组
        //boss线程组用于处理客户端的连接
        //worker线程组用于处理 channel的读写
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        //SereverBootstrap是Netty用于启动NIO服务端的封装类
        ServerBootstrap bootstrap = null;
        
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) //设置创建的Channel为NioServerSocketChannel,对用于JDK NIO类库中的ServerSocketChannel
                .option(ChannelOption.SO_BACKLOG, 1024) //设置TCP的参数
                .childHandler(new AcceptChannelHandler()); //绑定IO事件的处理类
            
            //绑定端口,同步等待绑定结束
            ChannelFuture channelFuture = bootstrap.bind(Commom.PORT).sync();
            System.out.println("Netty time server bind in port : " + Commom.PORT);
            
            //对main方法进行堵塞,等待服务端链路关闭之后,main方法退出
            //exceptionCaught()中关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出,释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class AcceptChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel arg0) throws Exception {
        arg0.pipeline().addLast(new TimeServerHandler());
    }
}

/**
 * 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月17日 下午12:21:54
 */
class TimeServerHandler extends ChannelHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf是JDK ByteBuffer的升级版,提供更方便的API
        ByteBuf byteBuf = (ByteBuf)msg;
        //byteBuf.readableBytes()返回缓冲区可供读取的字节数
        byte[] requestByte = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(requestByte);
        String request = new String(requestByte);
        System.out.println("Get order : " + request);
        String response = Commom.COMMAND.equals(request) ? new Date().toString() : "Bad order";
        //往client端写消息
        ByteBuf resp = Unpooled.copiedBuffer(response.getBytes());
        ctx.write(resp);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //为了效率,避免频繁唤醒"selector",Netty的write方法并不直接将消息写入到SocketChannel
        //调用write方法只是把待发送的消息发送到缓冲队列中,再通过调用flush
        //flush的作用是将"消息发送队列"中的消息写入SocketChannel中,发送到对方
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常,关闭ChannelHandlerContext
        //释放和ChannelHandlerContext相关联的句柄资源
        ctx.close();
    }
}
