package org.example.consumer;


import common.util.ConfigUtil;
import org.example.config.RpcConfig;


public class ConsumerTestConfig {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtil.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }

}
