package Client.serviceCenter;

import java.net.InetSocketAddress;


//服务中心接口
public interface ServiceCenter {
    //  查询：根据服务名查找地址
    InetSocketAddress serviceDiscovery(String serviceName);
}
