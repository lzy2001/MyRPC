package Server.server.impl;


import lombok.AllArgsConstructor;
import Server.server.RpcServer;
import Server.server.work.WorkThread;
import Server.provider.ServiceProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


@AllArgsConstructor
public class SimpleRPCRPCServer implements RpcServer {
    private ServiceProvider serviceProvide;
    @Override
    public void start(int port) {
        try {
            // 创建一个 ServerSocket 实例，用于在指定的 port 端口上监听客户端的连接请求
            ServerSocket serverSocket=new ServerSocket(port);
            System.out.println("服务器启动了");
            while (true) {
                //如果没有连接，会堵塞在这里
                Socket socket = serverSocket.accept();
                //有连接，创建一个新的线程执行处理
                new Thread(new WorkThread(socket,serviceProvide)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // 停止服务器
        // 可以在未来版本中优化服务端关闭的流程
    }
}
