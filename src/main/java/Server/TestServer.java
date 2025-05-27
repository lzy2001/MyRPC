package Server;

import Server.server.RpcServer;
import Common.service.Impl.UserServiceImpl;
import Common.service.UserService;
import Server.server.impl.NettyRPCServer;
import Server.provider.ServiceProvider;


public class TestServer {
    public static void main(String[] args) {
        // 创建服务实现类
        UserService userService=new UserServiceImpl();
        // 实例化服务注册中心，用于管理所有可供客户端调用的服务
        ServiceProvider serviceProvider=new ServiceProvider("127.0.0.1", 9999);
        // 注册服务
        serviceProvider.provideServiceInterface(userService, true);
        // 实例化服务端
        RpcServer rpcServer=new NettyRPCServer(serviceProvider);
        // 启动服务端
        rpcServer.start(9999);
    }
}