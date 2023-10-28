package cn.com.bling;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName:     
 * @Description: NIO客户端具体处理类
 * @author: bling
 * @date:        
 *   
 */
public class ClientHandler implements Runnable {

    private SocketChannel socketChannel;
    private int port;
    private Selector selector;
    private String host;
    private boolean stop;
    private Map<String,RpcResponse> cacheMap=new ConcurrentHashMap<>();


    public ClientHandler(String host, int port) {

        this.host = host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void run() {
        try {
            doConnect();// 连接
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 添加钩子, 在停止服务时关闭资源
     */
    public void close() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private void handleInput(SelectionKey key) throws IOException {
        //这个通道已经可以读取了
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (!sc.finishConnect()) {
                    System.out.println("没有成功建立连接......");
                    System.exit(1);
                }
            }
            if (key.isReadable()) {// 读取消息
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int read = sc.read(readBuffer);
                if (read > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    //进行反序列化
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    try {
                        RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
                        //进行请求的匹配
                        for (Map.Entry<String, RpcResponse> entry : cacheMap.entrySet()) {
                            if(rpcResponse.getRequestId().equals(entry.getKey())){
                                String lock = rpcResponse.getRequestId().intern();
                                synchronized (lock){
                                    cacheMap.put(entry.getKey(), rpcResponse);
                                    lock.notify();
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (read < 0) {
                    key.cancel();
                    sc.close();
                } else {
                }
            }

        }
    }

    private void doConnect() throws IOException {
        socketChannel.connect(new InetSocketAddress(host, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
    }

    private void doWrite(SocketChannel socketChannel, Object request)throws IOException {
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
            System.out.println("客户端发送命令成功");
        }
    }


    public void sendObj(Object msg) throws Exception{

        doWrite(socketChannel, msg);

    }

    public RpcResponse sendAndWaitReturn(RpcRequest rpcRequest) throws Exception{
        doWrite(socketChannel, rpcRequest);
        cacheMap.put(rpcRequest.getRequestId(),new RpcResponse("",null));
        //使用等待通知机制  等服务端进行返回
        String lock = rpcRequest.getRequestId().intern();
        synchronized (lock){
            //超时时间2s
            lock.wait(2000);
            RpcResponse rpcResponse = cacheMap.get(rpcRequest.getRequestId());
            if(rpcResponse.getData()!=null){
                cacheMap.remove(rpcRequest.getRequestId());
                return rpcResponse;
            }
            System.out.println("服务端返回超时......");
            return null;
        }
    }


}

