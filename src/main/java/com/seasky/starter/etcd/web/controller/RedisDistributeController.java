package com.seasky.starter.etcd.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
public class RedisDistributeController {

    @Autowired
    RedisDistributionService redisDistributionService;

    @RequestMapping(name = "/dis", method = RequestMethod.GET)
    public String distributedLockTest() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(10);

        for (int i = 0; i < 10; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startSignal.countDown();
                    Integer count = redisDistributionService.count();
                    System.out.println(Thread.currentThread().getName() + ": " + count);
                }
            }).start();
        }
        startSignal.await();

        return "finish";
    }

}
