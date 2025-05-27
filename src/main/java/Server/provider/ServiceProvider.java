package Server.provider;

import Server.rateLimit.provider.RateLimitProvider;
import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

//本地服务存放器
public class ServiceProvider {
    private final Map<String,Object> interfaceProvider;

    private final int port;
    private final String host;
    //注册服务类
    private final ServiceRegister serviceRegister;
    //限流器
    @Getter
    private RateLimitProvider rateLimitProvider;
    public ServiceProvider(String host,int port){
        //需要传入服务端自身的网络地址
        this.host=host;
        this.port=port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
        this.rateLimitProvider = new RateLimitProvider();
    }

    public void provideServiceInterface(Object service, boolean canRetry){
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();

        for (Class<?> clazz: interfaceName){
            //本机的映射表
            interfaceProvider.put(clazz.getName(), service);
            //在注册中心注册服务
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host,port), canRetry);
        }
    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }

}