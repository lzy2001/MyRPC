package Server;


import Server.server.RpcServer;
import Common.service.Impl.UserServiceImpl;
import Common.service.UserService;
import Server.server.impl.NettyRPCServer;
import Server.provider.ServiceProvider;


public class TestServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new NettyRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}