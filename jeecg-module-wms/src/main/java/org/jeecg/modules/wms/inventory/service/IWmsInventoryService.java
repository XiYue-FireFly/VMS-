package org.jeecg.modules.wms.inventory.service;

import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2025-11-26
 * @Version: V1.0
 */
public interface IWmsInventoryService extends IService<WmsInventory> {
    /**
     * 根据唯一键获取库存
     * @param productId
     * @param locationCode
     * @param batchNumber
     * @return
     */
    public WmsInventory getInventoryByUniqueKey(String productId, String locationCode, String batchNumber);

    /**
     * 根据SKU查询可用库存
     * @param warehouseId
     * @param item
     * @return
     */
    List<WmsInventory> selectAvailableBySku(String warehouseId, WmsOutOrdersItems item);
}
