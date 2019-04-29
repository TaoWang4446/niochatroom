package com.wang;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * NOI客户端
 */
public class NioClient {
    //启动
    public void start() throws IOException {
        // 连接服务器端
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",8000));
        //向服务器端发送数据
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()){
            String request = scanner.nextLine();
            if(request != null && request.length() > 0){
                socketChannel.write(Charset.forName("UTF-8").encode(request));
            }
        }
        // 接收服务器响应
    }

    public static void main(String[] args) throws IOException{
        new NioClient().start();
    }
}
