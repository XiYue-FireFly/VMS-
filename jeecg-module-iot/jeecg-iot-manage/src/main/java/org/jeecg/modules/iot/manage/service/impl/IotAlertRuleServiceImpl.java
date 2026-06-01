package org.jeecg.modules.iot.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.iot.base.mqtt.constant.CRUD;
import org.jeecg.modules.iot.base.mqtt.constant.EnableEnum;
import org.jeecg.modules.iot.base.mqtt.constant.IgnoreDurationEnum;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.config.TenantContext;
import org.jeecg.modules.iot.manage.entity.IotAlertRule;
import org.jeecg.modules.iot.manage.entity.IotDevice;
import org.jeecg.modules.iot.manage.mapper.IotAlertRuleMapper;
import org.jeecg.modules.iot.manage.service.CacheService;
import org.jeecg.modules.iot.manage.service.IIotAlertRuleService;
import org.jeecg.modules.iot.manage.service.IIotDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @Description: 报警规则表
 * @Author: jeecg-boot
 * @Date: 2025-05-03
 * @Version: V1.0
 */
@Slf4j
@Service
public class IotAlertRuleServiceImpl extends ServiceImpl<IotAlertRuleMapper, IotAlertRule> implements IIotAlertRuleService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private IIotDeviceService deviceService;

    @Override
    public <E extends IPage<IotAlertRule>> E page(E page, Wrapper<IotAlertRule> queryWrapper) {
        E pageRes = super.page(page, queryWrapper);
        List<IotAlertRule> records = pageRes.getRecords();
        records.stream().forEach(record ->
                record.setIgnoreDuration(IgnoreDurationEnum.getIdxBySec(record.getIgnoreDuration())));
        return pageRes;
    }

    @Override
    public IotAlertRule getById(Serializable id) {
        IotAlertRule rule = super.getById(id);
        rule.setIgnoreDuration(IgnoreDurationEnum.getIdxBySec(rule.getIgnoreDuration()));
        return rule;
    }

    @Override
    public boolean save(IotAlertRule entity) {
        entity.setTenantId(Integer.valueOf(TenantContext.getTenant()));
        entity.setIsEnable(EnableEnum.YES.getValue());
        entity.setIgnoreDuration(IgnoreDurationEnum.getSecByIdx(entity.getIgnoreDuration()));
        IotDevice device = deviceService.getOne(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getDeviceCode, entity.getDeviceCode()));
        entity.setSysOrgCode(device.getWarehouseCode());
        entity.setWarehouseName(device.getWarehouseName());

        // 新增规则要同步到 redis，供后续报警分析使用
        boolean saveRes = super.save(entity);
        return saveRes;
    }

    @Override
    public boolean updateById(IotAlertRule entity) {
        // 开关更新的情况
        if (!StringUtils.hasText(entity.getAlertName())) {
            return this.switchEnable(entity);
        }

        entity.setIgnoreDuration(IgnoreDurationEnum.getSecByIdx(entity.getIgnoreDuration()));

        return super.updateById(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        IotAlertRule rule = super.getById(id);
        boolean removed = super.removeById(id);
        return removed;
    }

    /**
     * 开关更新的场景
     *
     * @return
     */
    private boolean switchEnable(IotAlertRule entity) {
        IotAlertRule rule = super.getById(entity.getId());
        rule.setIsEnable(entity.getIsEnable());
        return super.updateById(entity);
    }
}
