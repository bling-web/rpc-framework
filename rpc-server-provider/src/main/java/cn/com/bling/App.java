package cn.com.bling;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        //启动服务端网络
        new Server().start();

        //发布服务
        ProcessorHandler.publisher(new IHelloServiceImpl());

    }
}
