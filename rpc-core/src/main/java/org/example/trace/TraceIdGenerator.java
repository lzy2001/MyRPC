package org.example.trace;

import java.util.UUID;


public class TraceIdGenerator {
    // 机器序列号默认为0，真实场景中从配置中心获取
    private static final SnowflakeIdGenerator SNOWFLAKE = new SnowflakeIdGenerator(0L);

    // 基于 Snowflake 算法 生成一个唯一的 Trace ID
    // 雪花算法保证了 时间有序性 和 全局唯一性。
    public static String generateTraceId() {
        return Long.toHexString(SNOWFLAKE.nextId());
    }

    // UUID 方法适合不关心有序性的场景
    public static String generateTraceIdUUID(){
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        // 去掉连字符
        String uuidWithoutHyphens = uuidString.replace("-", "");
        return uuidWithoutHyphens;
    }

    // 基于 当前时间戳 生成 Span ID（用于表示一次请求内部的一个操作）
    // 解释：很多系统中 generateSpanId() 使用时间戳是因为它：
    // - 简单快速，没有额外依赖；
    // - 人工调试时易读易理解；
    // - 某些场景对 Span ID 的唯一性要求不高（如内部日志追踪等）；
    public static String generateSpanId() {
        return String.valueOf(System.currentTimeMillis());
    }
    static class SnowflakeIdGenerator {
        // 机器 ID（0~1023）
        private final long workerId;

        // 基准时间（2021-01-01 00:00:00）
        private final long epoch = 1609459200000L;

        // 序列号（0~4095）
        private long sequence = 0L;

        // 上一次生成 ID 的时间戳
        private long lastTimestamp = -1L;

        // 构造函数，传入机器 ID
        public SnowflakeIdGenerator(long workerId) {
            if (workerId < 0 || workerId > 1023) {
                throw new IllegalArgumentException("Worker ID 必须在 0~1023 之间");
            }
            this.workerId = workerId;
        }

        // 生成下一个 ID
        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            // 如果当前时间小于上一次生成 ID 的时间，说明时钟回拨
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("时钟回拨！");
            }

            // 如果当前时间等于上一次生成 ID 的时间，递增序列号
            // 解释：在分布式系统中，同一台机器在同一毫秒内可能会有多个请求同时生成 ID。
            // 如果多次调用 nextId() 都发生在同一毫秒，
            // 那么 时间戳 和 workerId 也是一样的；需要靠 序列号 sequence 来区分每一个 ID。
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & 0xFFF; // 12 位序列号，最大 4095
                if (sequence == 0) {
                    // 如果序列号溢出，等待下一毫秒
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                // 如果当前时间大于上一次生成 ID 的时间，重置序列号
                sequence = 0L;
            }

            // 更新上一次生成 ID 的时间戳
            lastTimestamp = timestamp;

            // 生成 ID | 时间戳(41 位) | 机器 ID(10 位) | 序列号(12 位) |
            return ((timestamp - epoch) << 22) | (workerId << 12) | sequence;
        }

        // 等待下一毫秒
        private long waitNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}
