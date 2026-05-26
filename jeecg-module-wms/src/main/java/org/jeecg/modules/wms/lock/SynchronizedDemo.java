package org.jeecg.modules.wms.lock;

public class SynchronizedDemo {
    private int count = 0;

    private Object obj = new Object();

    // 同步方法 - 保证同一时间只有一个线程可以访问此方法
    public  void increment() {
        synchronized(obj){
            count++;
        }

    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedDemo demo = new SynchronizedDemo();
        demo.doWork();
    }

    public void doWork() throws InterruptedException {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    increment();
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    increment();
                }
            }
        });

        // 启动两个线程
        thread1.start();
        thread2.start();

        // 等待两个线程执行完成
        thread1.join();
        thread2.join();

        // 打印最终结果
        System.out.println("Count is: " + count);
    }
}
