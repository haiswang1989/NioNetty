package nio.mt.jdknio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2018年4月20日 下午5:06:42
 */
public class SampleServer {
    
    private static final int BUF_SIZE = 256;
    
    public static void main(String[] args) throws Exception {
        
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();
        //绑定端口
        ssChannel.socket().bind(new InetSocketAddress(8888));
        //非堵塞
        ssChannel.configureBlocking(false);
        
        String attachment = "test-attachment";
        //注册
        ssChannel.register(selector, SelectionKey.OP_ACCEPT, attachment);
        
        
        while(true) {
            int readyKeyCnt = 0;
            if((readyKeyCnt=selector.select()) == 0) {
                continue;
            }
            
            System.out.println("Ready key count : " + readyKeyCnt);
            
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                //必须删除,不删除下次遍历还会有
                keyIterator.remove();
                
                //连接事件,有client连接server了
                if(key.isAcceptable()) {
                    
                    //注意在OP_ACCEPT事件中,从key.channel()返回的是ServerSocketChannel
                    //而OP_WRITE和OP_READ事件中,从key.channel()返回的是SocketChannel
                    SocketChannel clientChannel = ((ServerSocketChannel)key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUF_SIZE));
                }
                
                if(key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel)key.channel();
                    ByteBuffer buf = (ByteBuffer)key.attachment();
                    long bytesRead = clientChannel.read(buf);
                    if(bytesRead == -1) {
                        //
                        clientChannel.close();
                    } else if(bytesRead > 0) {
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length : " + bytesRead);
                    }
                }
                
                if(key.isValid() && key.isWritable()) {
                    ByteBuffer buf = (ByteBuffer)key.attachment();
                    buf.flip();
                    
                    SocketChannel clientChannel = (SocketChannel)key.channel();
                    clientChannel.write(buf);
                    if(!buf.hasRemaining()) {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    
                    //压缩缓冲区
                    buf.compact();
                }
            }
        }
    }
    
}
