package cn.com.bling;

import org.omg.CORBA.OBJ_ADAPTER;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName:     
 * @Description: 反射具体执行类
 * @author: bling
 * @date:        
 *   
 */  
public class ProcessorHandler {

   public static final ConcurrentHashMap<Class<?>, Object> classMap = new ConcurrentHashMap<Class<?>, Object>();

    public static void publisher(Object publisher){
        //其实就是将这个实现类加到一个集合中
        //后面调用反射的时候,进行匹配,找到当前接口的实现类对象
        Class<?>[] interfaces = publisher.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            classMap.put(anInterface, publisher);
        }
    }

    public static RpcResponse invoke(RpcRequest rpcRequest){
        try {
            String requestId = rpcRequest.getRequestId();
            String classPath = rpcRequest.getClassPath();
            Class<?> aClass = Class.forName(classPath);
            if(!classMap.containsKey(aClass)){
                System.out.println("该接口并未暴露......"+aClass.getName());
                return null;
            }
            Object instance = classMap.get(aClass);
            //真正进行反射调用
            Method method = aClass.getMethod(rpcRequest.getMethodName(),rpcRequest.getTypes());
            Object result = method.invoke(instance, rpcRequest.getParameters());
            //封装返回结果
            return new RpcResponse(requestId,result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
