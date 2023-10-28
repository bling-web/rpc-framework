package cn.com.bling;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName:     
 * @Description: NIO服务端具体操作类
 * @author: bling
 * @date:        
 *   
 */
 public class ServerHandler implements Runnable {

     private Selector selector = null;
     private ServerSocketChannel serverChannel = null;
     private boolean stop;

     /**
      * 初始化多路复用器，绑定监听端口
      *
      * @param port
      */
     public ServerHandler(int port) {
         try {
             selector = Selector.open();
             serverChannel = ServerSocketChannel.open();
             serverChannel.configureBlocking(false);
             serverChannel.socket().bind(new InetSocketAddress(port), 1024);
             serverChannel.register(selector, SelectionKey.OP_ACCEPT);
             System.out.println("服务器监听" + port);
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }


     }

     public void stop() {
         this.stop = true;
     }

     public void run() {
         while (!stop) {
             try {
                 selector.select(1000);
                 Set<SelectionKey> selectionKeys = selector.selectedKeys();
                 Iterator<SelectionKey> it = selectionKeys.iterator();
                 SelectionKey key = null;
                 while (it.hasNext()) {
                     key = it.next();
                     it.remove();
                     try {
                         handleInput(key);
                     } catch (IOException e) {
                         if (key != null) {
                             key.cancel();
                             if (key.channel() != null)
                                 key.channel().close();
                         }
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }

         }
         if (selector != null) {
             try {
                 selector.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }

     /**
      * 处理事件
      * @param key
      * @throws IOException
      */
     private void handleInput(SelectionKey key) throws IOException {
         if (key.isValid()) {
             if (key.isAcceptable()) {
                 ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                 SocketChannel sc = ssc.accept();
                 sc.configureBlocking(false);
                 sc.register(selector, SelectionKey.OP_READ);
             }
             if (key.isReadable()) {
                 SocketChannel sc = (SocketChannel) key.channel();
                 ByteBuffer readBuff = ByteBuffer.allocate(1024);
                 //非阻塞的
                 int read = sc.read(readBuff);
                 if (read > 0) {
                     readBuff.flip();
                     byte[] bytes = new byte[readBuff.remaining()];
                     readBuff.get(bytes);
                     //转换成传输对象
                     ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                     ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                     try {
                         RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
                         RpcResponse rpcResponse = ProcessorHandler.invoke(rpcRequest);
                         //将得到的结果返回去
                         doWrite(sc,rpcResponse);
                     } catch (ClassNotFoundException e) {
                         e.printStackTrace();
                     }
                 } else if (read < 0) {
                     key.cancel();
                     sc.close();
                 }
             }
         }
     }




    /**
     * 异步发送应答消息
     * @throws IOException
     */
     static void doWrite(SocketChannel socketChannel, Object request)throws IOException {
         if(request==null){
             System.out.println("返回对象为null");
             return;
         }
        //进行序列化
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        ByteBuffer writeBuff = ByteBuffer.allocate(bytes.length);
        writeBuff.put(bytes);
        writeBuff.flip();
        socketChannel.write(writeBuff);

        if (!writeBuff.hasRemaining()) {
            System.out.println("服务端发送命令成功");
        }
    }
 }



