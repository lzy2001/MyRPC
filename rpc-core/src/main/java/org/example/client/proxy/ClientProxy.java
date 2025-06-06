package org.example.client.proxy;

import org.example.client.circuitBreaker.CircuitBreaker;
import org.example.client.circuitBreaker.CircuitBreakerProvider;
import org.example.client.rpcClient.RpcClient;
import org.example.client.retry.GuavaRetry;
import org.example.client.rpcClient.impl.NettyRpcClient;
import org.example.client.serviceCenter.ServiceCenter;
import org.example.client.serviceCenter.ZKServiceCenter;


import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.trace.interceptor.ClientTraceInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;


@Slf4j
public class ClientProxy implements InvocationHandler {
    // 传入参数service接口的class对象，反射封装成一个request

    private RpcClient rpcClient;
    private ServiceCenter serviceCenter;
    private CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy() throws InterruptedException {
        serviceCenter = new ZKServiceCenter();
        circuitBreakerProvider = new CircuitBreakerProvider();
    }

    // jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // trace 记录
        ClientTraceInterceptor.beforeInvoke();
        // 构建request
        RpcRequest request = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        // 获取熔断器
        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        // 判断熔断器是否允许请求经过
        if (!circuitBreaker.allowRequest()) {
            log.warn("熔断器开启，请求被拒绝: {}", request);
            //这里可以针对熔断做特殊处理，返回特殊值
            return null;
        }
        // 数据传输
        RpcResponse response;
        // 后续添加逻辑：为保持幂等性，只对白名单上的服务进行重试
        // 如果启用重试机制，先检查是否需要重试
        String methodSignature = getMethodSignature(request.getInterfaceName(), method);
        log.info("方法签名: " + methodSignature);
        InetSocketAddress serviceAddress = serviceCenter.serviceDiscovery(request);
        rpcClient = new NettyRpcClient(serviceAddress);
        if (serviceCenter.checkRetry(serviceAddress, methodSignature)) {
            //调用retry框架进行重试操作
            try {
                log.info("尝试重试调用服务: {}", methodSignature);
                response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
            } catch (Exception e) {
                log.error("重试调用失败: {}", methodSignature, e);
                circuitBreaker.recordFailure();
                throw e;  // 将异常抛给调用者
            }
        } else {
            // 只调用一次
            response = rpcClient.sendRequest(request);
        }
        // 记录response的状态，上报给熔断器
        if (response != null) {
            if (response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if (response.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", request.getInterfaceName(), response.getCode());
        }
        // trace 上报
        ClientTraceInterceptor.afterInvoke(method.getName());

        return response != null ? response.getData() : null;
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    // 根据接口名字和方法获取方法签名
    private String getMethodSignature(String interfaceName, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else{
                sb.append(")");
            }
        }
        return sb.toString();
    }

    // 关闭创建的资源
    // 注：如果在需要C-S保持长连接的场景下无需调用close方法
    public void close(){
        rpcClient.close();
        serviceCenter.close();
    }
}
