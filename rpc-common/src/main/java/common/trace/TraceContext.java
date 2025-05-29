package common.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;


@Slf4j
public class TraceContext {
    // MDC.put() 和 MDC.get() 是 ThreadLocal 实现的静态方法，并不依赖对象状态。这意味着：
    // MDC 中的数据是绑定到 当前线程 的； 无需创建 TraceContext 实例去调用；
    // 只需要一个静态工具类，就可以直接操作上下文。

    // 设置 / 获取当前线程的 traceId。
    public static void setTraceId(String traceId) {
        MDC.put("traceId",traceId);
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }

    // 设置 / 获取当前调用片段的 ID（span）。
    public static void setSpanId(String spanId) {
        MDC.put("spanId",spanId);
    }

    public static String getSpanId() {
        return MDC.get("spanId");
    }

    // 当前 span 的上层调用者 ID，帮助构建调用树。
    public static void setParentSpanId(String parentSpanId) {
        MDC.put("parentSpanId",parentSpanId);
    }

    public static String getParentSpanId() {
        return MDC.get("parentSpanId");
    }

    // 设置开始时间（通常用于计算调用耗时）。
    public static void setStartTimestamp(String startTimestamp) {
        MDC.put("startTimestamp",startTimestamp);
    }

    public static String getStartTimestamp() {
        return MDC.get("startTimestamp");
    }
    public static Map<String,String> getCopy(){
        return MDC.getCopyOfContextMap();
    }
    public static void clone(Map<String,String> context){
        for(Map.Entry<String,String> entry:context.entrySet()){
            MDC.put(entry.getKey(),entry.getValue());
        }
    }
    public static void clear() {
        MDC.clear();
    }
}