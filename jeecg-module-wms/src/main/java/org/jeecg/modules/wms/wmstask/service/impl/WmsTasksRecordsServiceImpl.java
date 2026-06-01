package org.jeecg.modules.wms.wmstask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksRecordsMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 任务执行记录表
 * @Author: jeecg-boot
 * @Date:   2025-11-23
 * @Version: V1.0
 */
@Service
public class WmsTasksRecordsServiceImpl extends ServiceImpl<WmsTasksRecordsMapper, WmsTasksRecords> implements IWmsTasksRecordsService {

    @Override
    public IPage<WmsTasksRecords> pageList(WmsTasksRecords wmsTasksRecords, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<WmsTasksRecords> queryWrapper = new LambdaQueryWrapper<>();
        // 可以根据需要添加查询条件
        if (StringUtils.isNotEmpty(wmsTasksRecords.getTaskId())) {
            queryWrapper.eq(WmsTasksRecords::getTaskId, wmsTasksRecords.getTaskId());
        }
        queryWrapper.orderByDesc(WmsTasksRecords::getCreateTime);
        return this.page(new Page<>(pageNo, pageSize), queryWrapper);
    }
}
