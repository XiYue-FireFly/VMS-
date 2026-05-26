package org.jeecg.modules.wms.analysis.service;

import org.jeecg.modules.wms.analysis.vo.TodoTask;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 首页大屏接口
 * @date 2025/8/13 19:02
 */
public interface BigscreenService {

    /**
     * 查询待办任务
     */
    public List<TodoTask> findTodoTaskList(String warehouseId);
}
