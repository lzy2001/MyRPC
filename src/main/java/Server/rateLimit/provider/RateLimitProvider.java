package Server.rateLimit.provider;

import Server.rateLimit.RateLimit;
import Server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;


public class RateLimitProvider {
    // 用于存储每个接口名称与对应的速率限制器实例之间的映射关系
    private Map<String, RateLimit> rateLimitMap = new HashMap<>();
    /*
        根据接口名称获取对应的速率限制器实例
        如果该接口的速率限制器实例不存在，则会创建一个新的实例并返回
     */
    public RateLimit getRateLimit(String interfaceName) {
        // 检查 rateLimitMap 中是否已经存在该接口的速率限制器实例
        if(!rateLimitMap.containsKey(interfaceName)) {
            // 如果没有则创建一个新的速率限制器实例
            // 这里我们使用 TokenBucketRateLimitImpl 实现类，假设它使用令牌桶进行速率限制
            RateLimit rateLimit = new TokenBucketRateLimitImpl(100,10);
            // 将新创建的速率限制器存入Map中，以接口为键
            rateLimitMap.put(interfaceName, rateLimit);
            // 返回新创建的速率限制器
            return rateLimit;
        }
        // 如果map中已经包含该接口的速率限制器实例，则直接返回现有实例
        return rateLimitMap.get(interfaceName);
    }
}