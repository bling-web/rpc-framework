package cn.com.bling;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        //建立新的NIO连接,为后续通信做准备
        Client client = new Client();
        client.start();

        //使用代理调用IHelloService接口中的hello方法
        //具体细节, 网络通信这些, 都在代理方法中完成
        RpcProxyClient rpcProxyClient = new RpcProxyClient();
        IHelloService iHelloService = rpcProxyClient.proxyClient(IHelloService.class, client);


        //使用返回的代理对象进行方法调用,也就是rpc
        System.out.println(iHelloService.hello("hello"));

    }
}
