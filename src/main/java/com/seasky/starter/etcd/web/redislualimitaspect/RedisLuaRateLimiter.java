package com.seasky.starter.etcd.web.redislualimitaspect;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RedisLuaRateLimiter {
    String msg() default "系统繁忙,请稍后再试.";
}
