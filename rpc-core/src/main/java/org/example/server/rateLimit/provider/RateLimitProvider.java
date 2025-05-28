package org.example.server.rateLimit.provider;

import org.example.server.rateLimit.RateLimit;
import org.example.server.rateLimit.impl.TokenBucketRateLimitImpl;
import lombok.extern.slf4j.Slf4j;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitProvider {
    // 用于存储每个接口名称与对应的速率限制器实例之间的映射关系
    private final Map<String, RateLimit> rateLimitMap = new ConcurrentHashMap<>();

    // 默认的限流桶容量和令牌生成速率
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_RATE = 100;

    // 提供限流实例
    // 根据接口名称获取对应的速率限制器实例
    // 如果该接口的速率限制器实例不存在，则会创建一个新的实例并返回
    public RateLimit getRateLimit(String interfaceName) {
        return rateLimitMap.computeIfAbsent(interfaceName, key -> {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(DEFAULT_RATE, DEFAULT_CAPACITY);
            log.info("为接口 [{}] 创建了新的限流策略: {}", interfaceName, rateLimit);
            return rateLimit;
        });
    }
}
