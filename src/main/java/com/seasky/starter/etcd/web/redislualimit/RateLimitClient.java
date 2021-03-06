package com.seasky.starter.etcd.web.redislualimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;

@Service
public class RateLimitClient {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Qualifier("rateLimitLua")
    @Resource
    RedisScript<Long> rateLimitLua;

    @Qualifier("rateLimitInitLua")
    @Resource
    RedisScript<Long> rateLimitInitLua;

    /**
     * 项目启动初始化每个接口的流量限制
     * @param key
     * @return
     */
    public Token initToken(String key){
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

}