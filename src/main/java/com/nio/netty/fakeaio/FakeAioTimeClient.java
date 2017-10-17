package com.nio.netty.fakeaio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.nio.netty.common.Commom;

public class FakeAioTimeClient {

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
            boolean flag = true;
            long count = 0;
            //这边是一个死循环不停的在发
            while(flag) {
                bw.write(Commom.COMMAND);
                bw.newLine();
                bw.flush();
                System.out.println(count++);
            }
            
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
