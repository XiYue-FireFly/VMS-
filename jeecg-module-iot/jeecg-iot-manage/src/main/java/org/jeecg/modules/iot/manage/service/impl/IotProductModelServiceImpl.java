package org.jeecg.modules.iot.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.iot.base.mqtt.constant.CRUD;
import org.jeecg.modules.iot.base.mqtt.exception.BusiException;
import org.jeecg.common.config.TenantContext;
import org.jeecg.modules.iot.manage.entity.IotAlertRule;
import org.jeecg.modules.iot.manage.entity.IotProductModel;
import org.jeecg.modules.iot.manage.mapper.IotAlertRuleMapper;
import org.jeecg.modules.iot.manage.mapper.IotProductModelMapper;
import org.jeecg.modules.iot.manage.service.CacheService;
import org.jeecg.modules.iot.manage.service.IIotProductModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 产品物模型
 * @Author: jeecg-boot
 * @Date: 2025-04-24
 * @Version: V1.0
 */
@Service
public class IotProductModelServiceImpl extends ServiceImpl<IotProductModelMapper, IotProductModel> implements IIotProductModelService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private IotAlertRuleMapper alertRuleMapper;

    @Override
    public boolean save(IotProductModel entity) {
        if (entity.getProductCode() == null) {
            throw new BusiException("缺少产品编码");
        }
        this.checkUnique(entity);
        entity.setTenantId(Integer.valueOf(TenantContext.getTenant()));
        return super.save(entity);
    }

    @Override
    public boolean updateById(IotProductModel entity) {
        if (entity.getProductCode() == null) {
            throw new BusiException("缺少产品编码");
        }
        this.checkUnique(entity);
        return super.updateById(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        IotProductModel model = super.getById(id);
        // 绑定报警规则的物模型不允许删除
        boolean exists = alertRuleMapper.exists(new LambdaQueryWrapper<IotAlertRule>().eq(IotAlertRule::getModelCode, model.getCode()));
        if (exists) {
            throw new BusiException("该功能已绑定报警规则！");
        }
        return super.removeById(id);
    }

    /**
     * 校验唯一
     * 同产品下
     *
     * @return
     */
    private void checkUnique(IotProductModel model) {
        LambdaQueryWrapper<IotProductModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IotProductModel::getModelName, model.getModelName())
                .or(query -> query.eq(IotProductModel::getCode, model.getCode()));
        List<IotProductModel> dbModels = super.list(queryWrapper);
        if (!CollectionUtils.isEmpty(dbModels)) {
            List<IotProductModel> models = dbModels.stream()
                    .filter(m -> !m.getId().equals(model.getId()))
                    .toList();
            // 存在可能重复的名称或编码
            if (!CollectionUtils.isEmpty(models)) {
                for (IotProductModel dbModel : models) {
                    if (dbModel.getCode().equals(model.getCode())) {
                        throw new BusiException("功能编码不能重复！");
                    }
                    // 同产品下名称不能重复
                    if (dbModel.getProductCode().equals(model.getProductCode())
                            && dbModel.getModelName().equals(model.getModelName())) {
                        throw new BusiException("同产品下功能名称不能重复！");
                    }
                }
            }
        }
    }
}
