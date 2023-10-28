package cn.com.bling;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @ClassName:     
 * @Description:rpc传输对象
 * @author: bling
 * @date:        
 *   
 */  
public class RpcRequest implements Serializable {
    private String requestId; //用于匹配请求
    private String classPath;
    private String methodName;
    private Class[] types;
    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getTypes() {
        return types;
    }

    public void setTypes(Class[] types) {
        this.types = types;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }


    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", classPath='" + classPath + '\'' +
                ", methodName='" + methodName + '\'' +
                ", types=" + Arrays.toString(types) +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}