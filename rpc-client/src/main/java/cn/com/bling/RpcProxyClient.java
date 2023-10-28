package cn.com.bling;

import java.lang.reflect.Proxy;

/**
 * @ClassName:     
 * @Description: 代理类客户端
 * @author: bling
 * @date:        
 *   
 */  
public class RpcProxyClient {




    public <T> T proxyClient(Class<T> interfaceClass,Client client){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},new RemoteHandler(client));
    }

}
