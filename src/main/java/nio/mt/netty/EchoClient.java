package nio.mt.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2018年4月23日 下午2:43:35
 */
public class EchoClient {

    public static void main(String[] args) {
        
        EventLoopGroup group = new NioEventLoopGroup(); // 1
        Bootstrap client = new Bootstrap(); 
        client.group(group)
              .channel(NioSocketChannel.class) // 2
              .option(ChannelOption.TCP_NODELAY, true) // 3
              .handler(new ChannelInitializer<SocketChannel>() { 
                  @Override
                  public void initChannel(SocketChannel ch) throws Exception {
                      ChannelPipeline p = ch.pipeline();
                      p.addLast(new EchoChannelHandler()); //4
                  }
              });
        
        try {
            ChannelFuture channelFuture = client.connect("127.0.0.1", 8888).sync(); // 5
            channelFuture.channel().closeFuture().sync(); // 6
        } catch (InterruptedException e) {
        } finally {
            group.shutdownGracefully(); // 7
        }
    }
}

class EchoChannelHandler extends ChannelHandlerAdapter {
    
}
