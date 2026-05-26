package org.jeecg.modules.wms.waybill.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersService;
import org.jeecg.modules.wms.waybill.service.IWmsWaybillService;
import org.jeecg.modules.wms.waybill.task.handler.SimpleThreadCallable;
import org.jeecg.modules.wms.waybill.task.handler.WaybillThreadCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description 定时任务类
 * @date 2025/9/13 10:10
 */
@Component
@Slf4j
public class WaybillTaskJob {

    @Autowired
    private IWmsWaybillService wmsWaybillService;

    @Autowired
    private IWmsOutOrdersService wmsOutOrdersService;

    @Resource(name="waybillThreadPool")
    private ThreadPoolExecutor waybillThreadPool;

    //任务是否完成的标记 true表示正在运行  false表示没有正在运行
    private  volatile  boolean isRunning  = false;

//    @Scheduled(cron = "0/2 * * * * ?")
    public void test1() {
        log.info("===================开始执行任务=================");
        //向线程池放入10个任务
        for (int i = 0; i < 10; i++) {
            SimpleThreadCallable simpleThreadCallable = new SimpleThreadCallable();
            waybillThreadPool.submit(simpleThreadCallable);
        }
    }

//    @Scheduled(cron = "0/2 * * * * ?")
    public void test2() {
        //防止重入
        if(isRunning){
            log.info("上一轮任务正在执行中，还没有完成直接返回");
            return ;
        }

        log.info("===================开始执行任务=================");
        isRunning=true;
        //定义一个List<Future> 存入任务对象
        List<Future< Boolean>> futureList = new java.util.ArrayList<>();
        //向线程池放入10个任务
        for (int i = 0; i < 10; i++) {
            SimpleThreadCallable simpleThreadCallable = new SimpleThreadCallable();
            Future future= waybillThreadPool.submit(simpleThreadCallable);
            futureList.add( future);
        }
        //判断这些任务是否完成
        for (Future<Boolean> future : futureList){
            //获取每个任务的执行结果
            try {
                //阻塞方法
                Boolean result = future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
               log.info("任务执行中出现异常");
               //取消任务
                future.cancel(true);
            }
        }
        //执行到这里说明上一轮任务执行完成了
        isRunning = false;
    }


    /**
     * 定时获取面单
     * 首先获取打包完成的波次，调用顺丰接口进行下单，获取面单 pdf并存储至minio
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void createWaybills() {

        //防止重入
        if(isRunning){
            log.info("上一轮任务正在执行中，还没有完成直接返回");
            return ;
        }

        log.info("===================开始执行任务=================");
        isRunning=true;
        try {
            //查询一批没有生成运单的订单
            //查询出库单状态为打包完成且未创建运单的出库单
            WmsOutOrders wmsOutOrders = new WmsOutOrders();
            wmsOutOrders.setStatus(WarehouseDictEnum.OUTBOUND_PACKED.getCode());
            wmsOutOrders.setCreatedWaybill("0");
            //todo:出库单表添加status、createdWaybill联合索引
            IPage<WmsOutOrders> wmsOutOrdersIPage = wmsOutOrdersService.queryList(wmsOutOrders, 1, 1000);
            List<WmsOutOrders> records = wmsOutOrdersIPage.getRecords();
            if(records.size()<=0){
                return;
            }

            //定义一个List<Future> 存入任务对象
            List<Future< Boolean>> futureList = new java.util.ArrayList<>();
            //向线程池放入10个任务
            for (WmsOutOrders orders: records) {
                //创建生成运单的任务,订单id、
                WaybillThreadCallable waybillThreadCallable = new WaybillThreadCallable(wmsWaybillService, orders.getId());
                Future future= waybillThreadPool.submit(waybillThreadCallable);
                futureList.add( future);
            }
            //判断这些任务是否完成
            for (Future<Boolean> future : futureList){
                //获取每个任务的执行结果
                try {
                    //阻塞方法
                    Boolean result = future.get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.info("任务执行中出现异常");
                    //取消任务
                    future.cancel(true);
                }
            }
        }finally {
            //执行到这里说明上一轮任务执行完成了
            isRunning = false;
        }

    }

}
