package com.seasky.starter.etcd.web.controller;

import com.seasky.starter.etcd.web.redisdistributelock.DistributedLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisDistributionService {

    @Autowired
    private RedissonClient redissonClient;

    private int count = 10;

    @DistributedLock(lockName = "name", lockNamePost = "last",lockNamePre = "pre")
    public Integer count() {
        return count--;
    }

    /**
     * 默认取第0个参数作为id属性作为key
     */
   /* @DistributedLock(param = "id", lockNamePost = ".lock")
    public Integer aspect(Person person) {
        RMap<String, Integer> map = redissonClient.getMap("distributionTest");

        Integer count = map.get("count");

        if (count > 0) {
            count = count - 1;
            map.put("count", count);
        }
        return count;
    }*/

    /**
     * 取第argNum个参数作为key
     * @param i
     * @return
     */
    @DistributedLock(argNum = 1, lockNamePost = ".lock")
    public Integer aspect(String i) {
        RMap<String, Integer> map = redissonClient.getMap("distributionTest");

        Integer count = map.get("count");

        if (count > 0) {
            count = count - 1;
            map.put("count", count);
        }

        return count;
    }

    /*@DistributedLock(lockName = "lock", lockNamePost = ".lock")
    public int aspect(Action<Integer> action) {
        return action.action();
    }*/
}
