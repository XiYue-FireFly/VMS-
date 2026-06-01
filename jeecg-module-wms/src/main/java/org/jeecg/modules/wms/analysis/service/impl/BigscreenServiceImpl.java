package org.jeecg.modules.wms.analysis.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.wms.analysis.mapper.BigScreenMapper;
import org.jeecg.modules.wms.analysis.service.BigscreenService;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大屏的服务类
 * @date 2025/12/5 15:15
 */
@Service
@Slf4j
public class BigscreenServiceImpl implements BigscreenService {

    @Autowired
    private BigScreenMapper bigScreenMapper;



    /**
     * 统计当前天的待办任务
     * 数据格式：
     * //        List<TodoTask> todoTaskList = Arrays.asList(
     * //                new TodoTask("待收货任务","visit-count|svg",10,5,"当天"),
     * //                new TodoTask("待上架任务","total-sales|svg",10,5,"当天"),
     * //                new TodoTask("待拣货任务","download-count|svg",10,5,"当天"),
     * //                new TodoTask("待发货任务","download-count|svg",10,5,"当天")
     * //        );
     * @param warehouseId
     * @return
     */
    @Override
    @Cacheable(value = "sys:cache:bigscreen:todotasklist",key = "#warehouseId",cacheManager = "cacheManager30Minutes")
    public List<TodoTask> findTodoTaskList(String warehouseId) {
        //所有待收货任务总量
        int totalReceivingTaskCount = 0;
        //所有待上架任务总量
        int totalPutawayTaskCount = 0;
        //所有待拣货任务总量
        int totalPickingTaskCount = 0;

        //已完成收货任务数量
        int completedReceivingTaskCount = 0;
        //已完成上架任务数量
        int completedPutawayTaskCount = 0;
        //已完成拣货任务数量
        int completedPickingTaskCount = 0;

        //先统计当前的总的任务数量
        WmsTasks wmsTasks = new WmsTasks();
        //仓库 id
        wmsTasks.setTargetWarehouseId(warehouseId);
        //当前时间 需要格式为 yyyy-MM-dd
//        String time = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
//        wmsTasks.setCreateTimeString( time);
        List<WmsTasks> allTodoTaskList  = bigScreenMapper.countTaskList(wmsTasks);
        //遍历allTodoTaskList,向任务总数的三个变量赋值
        for (WmsTasks wmsTask : allTodoTaskList) {
            switch (wmsTask.getTaskType()) {
                case "RECEIVING_TASK":
                    totalReceivingTaskCount = wmsTask.getTaskCount();
                    break;
                case "PUTAWAY_TASK":
                    totalPutawayTaskCount = wmsTask.getTaskCount();
                    break;
                case "PICKING_TASK":
                    totalPickingTaskCount = wmsTask.getTaskCount();
            }
        }


        //再统计已完成的数量
        //在原有条件上添加任务状态为完成
        wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
        List<WmsTasks> completedTaskList = bigScreenMapper.countTaskList(wmsTasks);
        //遍历allTodoTaskList,向已完成数据的三个变量赋值
        for (WmsTasks wmsTask : completedTaskList) {
            switch (wmsTask.getTaskType()) {
                case "RECEIVING_TASK":
                    completedReceivingTaskCount = wmsTask.getTaskCount();
                    break;
                case "PUTAWAY_TASK":
                    completedPutawayTaskCount = wmsTask.getTaskCount();
                    break;
                case "PICKING_TASK":
                    completedPickingTaskCount = wmsTask.getTaskCount();
            }
        }

        //计算出待办任务数量
        int todoReceivingTaskCount = totalReceivingTaskCount - completedReceivingTaskCount;
        int todoPutawayTaskCount = totalPutawayTaskCount - completedPutawayTaskCount;
        int todoPickingTaskCount = totalPickingTaskCount - completedPickingTaskCount;

        //        List<TodoTask> todoTaskList = Arrays.asList(
//                new TodoTask("待收货任务","visit-count|svg",10,5,"当天"),
//                new TodoTask("待上架任务","total-sales|svg",10,5,"当天"),
//                new TodoTask("待拣货任务","download-count|svg",10,5,"当天"),
//                new TodoTask("待发货任务","download-count|svg",10,5,"当天")
//        );

        List<TodoTask> todoTaskList = Arrays.asList(
                new TodoTask("待收货任务","visit-count|svg",todoReceivingTaskCount,completedReceivingTaskCount,"当天"),
                new TodoTask("待上架任务","total-sales|svg",todoPutawayTaskCount,completedPutawayTaskCount,"当天"),
                new TodoTask("待拣货任务","download-count|svg",todoPickingTaskCount,completedPickingTaskCount,"当天"),
                new TodoTask("待发货任务","download-count|svg",0,0,"当天")
        );

        return todoTaskList;
    }
}
