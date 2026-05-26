package org.jeecg.modules.wms.wmstask.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date:   2025-11-23
 * @Version: V1.0
 */
public interface IWmsTasksService extends IService<WmsTasks> {
    /**
     * 创建收货任务
     * @param orderId 入库单id
     * @param operator 执行人
     */
    void createReceiveTask(String orderId, String operator);

    /**
     * 查询待处理任务
     * @param wmsTasks
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize);

    /**
     * 执行收货
     * @param wmsTasksRecords
     */
    void receive(WmsTasksRecords wmsTasksRecords);

    /**
     * 执行任务 上架、收货、拣货都要使用此方法
     */
    public WmsTasks execute(WmsTasksRecords wmsTasksRecords);
}
