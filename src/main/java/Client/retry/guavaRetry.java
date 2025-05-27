package Client.retry;

import com.github.rholder.retry.*;
import Client.rpcClient.RpcClient;
import Common.Message.RpcRequest;
import Common.Message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class guavaRetry {
    // 用来发送RPC请求
    private RpcClient rpcClient;
    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 无论出现什么异常，都进行重试
                .retryIfException()
                // 返回结果为 error 时进行重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // 重试等待策略：等待 2s 后再进行重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试停止策略：重试达到 3 次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 用于为 Retry 配置一个重试监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                    }
                }).build();
        try {
            // retryer.call执行PRC请求，要求重试
            // 传入的 lambda 表达式是要执行的操作，即rpcClient.sendRequest(request)，这会发送请求并返回一个 RpcResponse 对象
            return retryer.call(() -> rpcClient.sendRequest(request));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RpcResponse.fail();
    }
}