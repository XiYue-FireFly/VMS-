package org.jeecg.modules.wms.analysis.mapper;

import org.jeecg.modules.wms.wmstask.entity.WmsTasks;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大屏的数据接口
 * @date 2025/12/5 15:08
 */
public interface BigScreenMapper {

    /**
     * 统计待办任务
     */
    List<WmsTasks> countTaskList(WmsTasks wmsTasks);
}
