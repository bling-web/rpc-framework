package cn.com.bling;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @ClassName:     
 * @Description: 具体代理实现类
 * @author: bling
 * @date:        
 *   
 */  
public class RemoteHandler implements InvocationHandler {

    private Client client;

    public RemoteHandler(Client client) {
        this.client = client;
    }

    /**
     * 序列化---直接使用Java的序列化协议
     * 网络通信---NIO
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //封装传输对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassPath(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);
        //通过网络传输调用
        //返回执行远程方法后的结果
        RpcResponse rpcResponse = client.sendObjAndWaitResponse(rpcRequest);
        return rpcResponse == null ? null : rpcResponse.getData();
    }

}
