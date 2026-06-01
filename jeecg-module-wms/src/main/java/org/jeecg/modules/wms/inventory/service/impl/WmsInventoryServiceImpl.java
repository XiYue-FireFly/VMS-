package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2025-11-26
 * @Version: V1.0
 */
@Service
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory> implements IWmsInventoryService {

    @Override
    public WmsInventory getInventoryByUniqueKey(String productId, String locationCode, String batchNumber) {
        //库存唯一：商品id、储位编码、批号
        LambdaQueryWrapper<WmsInventory> eq = new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getProductId, productId)
                .eq(WmsInventory::getLocationCode, locationCode)
                .eq(StringUtils.isNotEmpty(batchNumber), WmsInventory::getBatchNumber, batchNumber)
                .eq(StringUtils.isEmpty(batchNumber), WmsInventory::getBatchNumber, "");
        return this.getOne(eq);
    }

    @Override
    public List<WmsInventory> selectAvailableBySku(String warehouseId, WmsOutOrdersItems item) {
        LambdaQueryWrapper<WmsInventory> queryWrapper = new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getWarehouseId, warehouseId)
                .eq(WmsInventory::getProductId, item.getSkuId())
                .gt(WmsInventory::getAvailableQuantity, 0)
                .orderByAsc(WmsInventory::getExpiryDate)
                .orderByAsc(WmsInventory::getCreateTime);
        return this.list(queryWrapper);
    }
}
