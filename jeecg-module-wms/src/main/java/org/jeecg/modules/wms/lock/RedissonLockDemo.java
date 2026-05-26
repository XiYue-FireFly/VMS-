package org.jeecg.modules.wms.lock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissonLockDemo {

    // 模拟商品库存
    private static int stock = 10;

    public static void main(String[] args) {
        // 1. 配置Redisson客户端
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://127.0.0.1:6379")
              .setDatabase(0);

        // 2. 创建Redisson客户端
        RedissonClient redissonClient = Redisson.create(config);

        // 模拟多个线程并发扣减库存
        for (int i = 0; i < 15; i++) {
            new Thread(() -> {
                String threadName = Thread.currentThread().getName();

                // 3. 获取分布式锁实例
                RLock lock = redissonClient.getLock("stockLock");

                try {
                    System.out.println(threadName + " 尝试获取锁...");

                    // 4. 尝试获取锁，最多等待1秒，锁持有时间为20秒
                    boolean isLockAcquired = lock.tryLock(1, -1, TimeUnit.SECONDS);

                    if (isLockAcquired) {
                        try {
                            System.out.println(threadName + " 获取锁成功，开始处理业务");

                            // 模拟业务处理
                            if (stock > 0) {
                                stock--;
                                System.out.println(threadName + " 扣减库存成功，剩余库存: " + stock);
                            } else {
                                System.out.println(threadName + " 库存不足，无法扣减");
                            }

                            // 模拟业务处理时间
                            Thread.sleep(60000);
                        } finally {
                            // 5. 释放锁
                            lock.unlock();
                            System.out.println(threadName + " 释放锁");
                        }
                    } else {
                        System.out.println(threadName + " 获取锁失败，放弃操作");
                    }
                } catch (InterruptedException e) {
                    System.out.println(threadName + " 被中断");
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i).start();
        }

        // 主线程等待所有子线程完成
        try {
            Thread.sleep(150000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. 关闭Redisson客户端
        redissonClient.shutdown();
        System.out.println("最终库存: " + stock);
    }
}
