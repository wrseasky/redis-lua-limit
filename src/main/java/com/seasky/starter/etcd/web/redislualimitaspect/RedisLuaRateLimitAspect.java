package com.seasky.starter.etcd.web.redislualimitaspect;

import com.seasky.starter.etcd.web.redislualimit.Constants;
import com.seasky.starter.etcd.web.redislualimit.Token;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
@Aspect
public class RedisLuaRateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RedisLuaRateLimitAspect.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Qualifier("rateLimitLua")
    @Resource
    RedisScript<Long> rateLimitLua;

    @Qualifier("rateLimitInitLua")
    @Resource
    RedisScript<Long> rateLimitInitLua;


    @Pointcut("@annotation(com.seasky.starter.etcd.web.redislualimitaspect.RedisLuaRateLimiter)")
    public void redisLuaRateLimiter() {
    }

    @Around("redisLuaRateLimiter()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        String url = request.getRequestURI();
        Token token = accquireToken(url, 1);
        logger.info("<<=================  请求{},获取Token{} ", url, token);

        RedisLuaRateLimiter rateLimiter = getAnRateLimiter(joinPoint);
        if (!Token.SUCCESS.equals(token)) {
            responseResult(response, 500, rateLimiter == null ? "" : rateLimiter.msg());
            return null;
        }

        return joinPoint.proceed();
    }

    /**
     * 项目启动初始化每个接口的流量限制
     *
     * @param key
     * @return
     */
    public Token initToken(String key) {
        Token token = Token.SUCCESS;
        Long currMillSecond = stringRedisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );

        /**
         * redis.pcall("HMSET",KEYS[1],
         "last_mill_second",ARGV[1],
         "curr_permits",ARGV[2],
         "max_burst",ARGV[3],
         "rate",ARGV[4],
         "app",ARGV[5])
         */
        Long accquire = stringRedisTemplate.execute(rateLimitInitLua,
                Collections.singletonList(getKey(key)), currMillSecond.toString(), "1", "10", "10", "skynet");
        if (accquire == 1) {
            token = Token.SUCCESS;
        } else if (accquire == 0) {
            token = Token.SUCCESS;
        } else {
            token = Token.FAILED;
        }
        return token;
    }


    /**
     * 访问接口时尝试获得key
     *
     * @param key
     * @return
     */
    public Token accquireToken(String key) {
        return accquireToken(key, 1);
    }

    public Token accquireToken(String key, Integer permits) {
        Token token = Token.SUCCESS;
        Long currMillSecond = stringRedisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );
        System.out.println(new Date(currMillSecond));

        Long accquire = stringRedisTemplate.execute(rateLimitLua,
                Collections.singletonList(getKey(key)), permits.toString(), currMillSecond.toString());
        if (accquire == 1) {
            token = Token.SUCCESS;
        } else {
            token = Token.FAILED;
        }
        return token;
    }

    public String getKey(String key) {
        return Constants.RATE_LIMIT_KEY + key;
    }


    /**
     * 获取注解对象
     *
     * @param joinPoint 对象
     * @return ten LogAnnotation
     */
    private RedisLuaRateLimiter getAnRateLimiter(final JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getDeclaredAnnotation(RedisLuaRateLimiter.class);
    }

    /**
     * 自定义响应结果
     *
     * @param response 响应
     * @param code     响应码
     * @param message  响应信息
     */
    private void responseResult(HttpServletResponse response, Integer code, String message) {
        response.resetBuffer();
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.println("{\"code\":" + code + " ,\"message\" :\"" + message + "\"}");
            response.flushBuffer();
        } catch (IOException e) {
            logger.error(" 输入响应出错 e = {}", e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

}