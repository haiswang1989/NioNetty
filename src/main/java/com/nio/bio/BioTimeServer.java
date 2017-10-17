package com.nio.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.nio.common.Commom;
import com.nio.common.TimeServerHandler;

/**
 * 堵塞式
 * 
 * 缺点：
 * 这个方式的缺点就是会为每一个连接创建一个线程
 * 在Java中创建一个线程的代价还是很高的
 * 如果在搞并发的场景下,创建上万个线程还是不现实的
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月12日 上午11:51:13
 */
public class BioTimeServer {

    public static void main(String[] args) {
        System.out.println("Start time server...");
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Commom.PORT);
            while(true) {
                Socket socket = ss.accept();
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(null!=ss) {
                    ss.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
