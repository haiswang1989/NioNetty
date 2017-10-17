package com.nio.bio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.nio.common.Commom;

public class BioTimeClient {

    public static void main(String[] args) {
        Socket socket = null;
        
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            socket = new Socket("127.0.0.1", Commom.PORT);
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));
            br = new BufferedReader(new InputStreamReader(is));
            bw.write(Commom.COMMAND);
            //写完命令以后,必须写一个换行然后在flush一下
            //不然服务端无法收到command,只能等到client端写的数据越来越多,超过了缓冲区的大小以后,系统自动的flush
            //同理服务端往客户端写结果的时候,也需要这样调用
            bw.newLine();
            bw.flush();
            System.out.println("Send command success.");
            String response = br.readLine();
            System.out.println("response : " + response);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null!=br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(null!=bw) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
