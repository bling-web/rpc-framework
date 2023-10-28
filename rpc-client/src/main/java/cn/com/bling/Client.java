package cn.com.bling;

/**
 * @ClassName:     
 * @Description: NIO客户端
 * @author: bling
 * @date:        
 *   
 */


public class Client {

    private ClientHandler clientHandler;

    private String DEFAULT_HOST = "127.0.0.1";

    private int DEFAULT_PORT = 8081;

    public void start(){
        start(DEFAULT_HOST,DEFAULT_PORT);
    }



    public synchronized void start(String ip,int port){
        clientHandler = new ClientHandler(ip,port);
        new Thread(clientHandler,"Server").start();
        clientHandler.close();
    }


    //向服务器发送消息
    public  boolean sendObj(Object obj) throws Exception{
        clientHandler.sendObj(obj);
        return true;

    }

    //向服务器发送消息并等待返回
    public RpcResponse sendObjAndWaitResponse(RpcRequest obj) throws Exception{
        return clientHandler.sendAndWaitReturn(obj);

    }

}

