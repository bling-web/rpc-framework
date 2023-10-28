package cn.com.bling;



/**
 * @ClassName:
 * @Description: IHelloService实现类
 * @author: bling
 * @date:
 *
 */
public class IHelloServiceImpl implements IHelloService {
    @Override
    public String hello(String str) {
        return "com.bling.rpc---"+str;
    }
}
