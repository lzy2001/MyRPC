package org.example;


import org.example.config.RpcConfig;
import org.example.config.RpcConstant;
import common.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfigInstance;

    public static void initialize(RpcConfig customRpcConfig) {
        rpcConfigInstance = customRpcConfig;
        log.info("RPC 框架初始化，配置 = {}", customRpcConfig);
    }

    public static void initialize() {
        RpcConfig customRpcConfig;
        try {
            customRpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.CONFIG_FILE_PREFIX);
            log.info("成功加载配置文件，配置文件名称 = {}", RpcConstant.CONFIG_FILE_PREFIX); // 添加成功加载的日志
        } catch (Exception e) {
            // 配置加载失败，使用默认配置
            customRpcConfig = new RpcConfig();
            log.warn("配置加载失败，使用默认配置");
        }
        initialize(customRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfigInstance == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfigInstance == null) {
                    initialize();  // 确保在第一次调用时初始化
                }
            }
        }
        return rpcConfigInstance;
    }
}
