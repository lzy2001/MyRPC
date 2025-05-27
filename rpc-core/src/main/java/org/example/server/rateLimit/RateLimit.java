package org.example.server.rateLimit;


public interface RateLimit {
    //获取访问许可
    boolean getToken();
}
