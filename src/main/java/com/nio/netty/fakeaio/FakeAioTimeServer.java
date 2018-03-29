//package com.nio.netty.fakeaio;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//import com.nio.netty.common.Commom;
//import com.nio.netty.common.TimeServerHandler;
//import com.nio.netty.common.TimeServerHandlerExecutePool;
//
///**
// * 伪异步的方式
// * 通过构造一个线程池,来进行处理,避免创建太多的线程
// * 
// * 缺点：
// * read()方法是堵塞的,在内核准备数据的时候,该处理线程只能堵塞在read()方法处
// * write()方法,如果read()方法堵塞住,那么write()就不能再发了(write的空间变成了0,只有read读了以后空间不再是0才能继续write)
// * 
// * <p>Description:</p>
// * @author hansen.wang
// * @date 2017年10月12日 上午11:52:12
// */
//public class FakeAioTimeServer {
//
//    public static void main(String[] args) {
//        //创建一个线程池
//        TimeServerHandlerExecutePool executePool = new TimeServerHandlerExecutePool(10, 10);
//        
//        System.out.println("Start time server...");
//        ServerSocket ss = null;
//        try {
//            ss = new ServerSocket(Commom.PORT);
//            while(true) {
//                Socket socket = ss.accept();
//                executePool.execute(new TimeServerHandler(socket));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if(null!=ss) {
//                    ss.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
