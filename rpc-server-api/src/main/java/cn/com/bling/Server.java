package cn.com.bling;

/**
 * @ClassName:     
 * @Description:NIO服务端
 * @author: bling
 * @date:        
 *   
 */

public class Server {

    private static int DEFAULT_PORT = 8081;
    private ServerHandler serverHandler;

    public void start() {
        start(DEFAULT_PORT);
    }

    public synchronized void start(int port) {
        serverHandler = new ServerHandler(port);
        new Thread(serverHandler, "Server").start();
    }


}


