package org.jeecg.modules.wms.wmstask.service;

import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
