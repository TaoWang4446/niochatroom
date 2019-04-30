package com.wang.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Nio 服务器端
 */
public class NioServer {

    /**
     * 启动方法
     */
    public void start() throws IOException {
        //1.创建一个Selector
        Selector selector = Selector.open();

        //2.通过ServerSocketChannel 创建 channel 通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3.为channel通道绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8000));//此时所有的客户端接入的时候都会由这个serverSocketChannel来处理。

        //4.设置channel为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //5.将channel注册到selector上，监听连接事件
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        System.out.println("服务器端启动成功！");

        //6.循环等待新接入的连接
        for(;;){ //while(true)  c  for;;
            int readChannels = selector.select();//是一个阻塞方法，返回是一个可用的channel的数量
            if(readChannels == 0) continue;//为什么要这样做？
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();//获取可用channel的集合
            Iterator iterator = selectionKeySet.iterator();
            while(iterator.hasNext()){
                //selectionKey实例（准备就绪的对象）
                SelectionKey selectionKey = (SelectionKey) iterator.next();
                iterator.remove();//移除selectionKeySet中的当前selectionKey【因为：selector在监听到一个channel就绪的情况后
                //selector,SelectionKey.OP_ACCEPT 会把它单独的放到一个set集合里
                // {Set<SelectionKey> selectionKeySet = selector.selectedKeys();}然后调用.selectedKeys()方法获取channel集合
                // 下次如果selector又检查到channel就绪的情况之后，还会把它放进去，所以在拿到channel之后，并且开始处理这个selectionKey
                // 就需要先把它从这个集合中移除，否则下次检查到之后，还是会set一遍，那这个集合selectionKeySet就会越来越多】


                /**
                 * 7.根据就绪状态【接入事件、可读事件；不同的就绪状态两种】，调用对应的方法处理业务逻辑
                 */
                //如果是 接入事件
                if(selectionKey.isAcceptable()){
                    acceptHandler(serverSocketChannel,selector);
                }


                //如果是 可读事件

                if(selectionKey.isReadable()){
                    readHandler(selectionKey,selector);
                }

            }
        }


    }

    //7.根据就绪状态，调用对应方法处理业务逻辑

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel,Selector selector) throws IOException{
        //如果是接入事件，创建一个socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        //将socketChannel设置为非阻塞工作模式
        socketChannel.configureBlocking(false);

        //将channel注册到selector上，监听 可读事件
        socketChannel.register(selector,SelectionKey.OP_READ);

        //回复客户端提示信息
        socketChannel.write(Charset.forName("UTF-8").encode("你和聊天室的人员都不是朋友关系，请注意隐私！"));
    }



    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException{
        //要从selectionKey中获取到已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        //创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //循环读取客户端的请求信息
        String request = "";
        while(socketChannel.read(byteBuffer) > 0){//读到buffer中多少个字节【socketChannel.read(byteBuffer) 】
            //切换buffer为读模式
            byteBuffer.flip();
            //读取buffer中的内容
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }
        //将channel再次注册到selector上，监听它的可读事件
        socketChannel.register(selector,SelectionKey.OP_READ);
        //将客户端发送的请求信息 广播给其他客户端
        if(request.length() > 0){
            System.out.println(":: "+request);
        }
    }

    public static void main(String[] args) throws IOException{
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
