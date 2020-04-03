package com.seasky.starter.etcd.web.controller;

import com.seasky.starter.etcd.web.limit.AnRateLimiter;
import com.seasky.starter.etcd.web.redislualimitaspect.RedisLuaRateLimiter;
import com.seasky.starter.etcd.web.redislualimit.RateLimitClient;
import com.seasky.starter.etcd.web.redislualimit.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class RedisLuaController {


    /**
     * 普通的并发限流
     * 非分布式，只能控制单个tomcat应用
     * @return
     */
    @GetMapping("/index")
    @AnRateLimiter(permitsPerSecond = 100, timeout = 500,
            timeunit = TimeUnit.MILLISECONDS, msg = "亲,现在流量过大,请稍后再试.")
    public String index01() {
        return System.currentTimeMillis() + "";
    }

    @Autowired
    RateLimitClient rateLimitClient;

    /**
     * 分布式限流
     * 首先初始化限流的url参数，设置到redis中
     * @return
     */
    @GetMapping("/intAspect")
    public String intAspect() {
        String key = "/aspect";
        Token token = rateLimitClient.initToken(key);
        return System.currentTimeMillis() + ": " + token;
    }

    /**
     * redis---lua 分布式限流
     * @return
     */
    @GetMapping("/get")
    public String get02() {
        String key = "get";
        Token token1 = rateLimitClient.accquireToken(key);
        return System.currentTimeMillis() + ": " + token1;
    }

    /**
     * redis---lua 分布式限流
     * 使用切面直接拦截
     * @return
     */
    @GetMapping("/aspect")
    @RedisLuaRateLimiter(msg = "系统忙不过来了")
    public String aspect03() {
        return System.currentTimeMillis() + "";
    }


}
