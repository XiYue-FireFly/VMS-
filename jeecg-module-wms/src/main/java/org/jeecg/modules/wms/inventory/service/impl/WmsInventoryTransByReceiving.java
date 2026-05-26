package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.entity.WmsInventoryTrans;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryTransMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryTransService;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageLocationsService;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 收货时执行库存变更
 */
@Service
public class WmsInventoryTransByReceiving extends ServiceImpl<WmsInventoryTransMapper, WmsInventoryTrans> implements IWmsInventoryTransService {

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    //注入储位表service
    @Autowired
    private IWmsStorageLocationsService wmsStorageLocationsService;

    //注入储区service
    @Autowired
    private IWmsStorageZonesService wmsStorageZonesService;

    //注入商品service
    @Autowired
    private IWmsProductsService wmsProductsService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(WmsInventoryTransParam inventoryTransParam) {
        //非空校验 商品id、仓库id、储位编码  是否可售
        if(StringUtils.isEmpty(inventoryTransParam.getProductId())
        || StringUtils.isEmpty(inventoryTransParam.getWarehouseId())
        || StringUtils.isEmpty(inventoryTransParam.getTargetLocationCode())
        || StringUtils.isEmpty(inventoryTransParam.getIsSellable())){
            throw new RuntimeException("商品id、仓库id、储位编码  是否可售不能为空");
        }
        //根据inventoryTransParam.getTargetLocationCode()查询储位
        WmsStorageLocations wmsStorageLocations = wmsStorageLocationsService.getOne(new LambdaQueryWrapper<WmsStorageLocations>().eq(WmsStorageLocations::getLocationCode, inventoryTransParam.getTargetLocationCode()));
        if(wmsStorageLocations == null){
            throw new RuntimeException("目标储位不存在");
        }
        //查询商品
        WmsProducts wmsProducts = wmsProductsService.getById(inventoryTransParam.getProductId());
        if(wmsProducts == null){
            throw new RuntimeException("商品不存在");
        }
        //查询库存
        WmsInventory inventoryByUniqueKey = wmsInventoryService.getInventoryByUniqueKey(inventoryTransParam.getProductId(), inventoryTransParam.getSourceLocationCode(), inventoryTransParam.getBatchNumber());
        //是否可售 1：可售，0:不可售
        String isSellable = inventoryTransParam.getIsSellable();
        //如果库存不存在则向库存表新增一条记录
        if(inventoryByUniqueKey == null){
            WmsInventory inventory = new WmsInventory();
            BeanUtils.copyProperties(inventoryTransParam,inventory);
            inventory.setLocationCode(inventoryTransParam.getTargetLocationCode());
            //在库数量
            inventory.setStockQuantity(inventoryTransParam.getExecQuantity());
            //分配数量为0
            inventory.setAllocatedQuantity(0);
            //如果可售设置可用数量
            if("1".equals(isSellable)){
                inventory.setAvailableQuantity(inventoryTransParam.getExecQuantity());
            }else{
                inventory.setAvailableQuantity(0);
            }
            //入库时间为当前时间
            inventory.setStockInTime(new Date());
            //货主id
            inventory.setOwnerId(wmsProducts.getOwnerId());

            boolean save = wmsInventoryService.save(inventory);
            if(!save){
                throw new RuntimeException("新增库存失败");
            }

        }else{
            //如果存在库存就在原有库存基础上增加库存
            //如果是良品要增加可用数量
            LambdaUpdateWrapper<WmsInventory> wmsInventoryLambdaUpdateWrapper = new LambdaUpdateWrapper<WmsInventory>()
                    .eq(WmsInventory::getId, inventoryByUniqueKey.getId())
                    .setSql("stock_quantity = stock_quantity + " + inventoryTransParam.getExecQuantity())//在库数量
                    .setSql(isSellable.equals("1"), "available_quantity = available_quantity + " + inventoryTransParam.getExecQuantity());
            boolean update = wmsInventoryService.update(wmsInventoryLambdaUpdateWrapper);
            if(!update){
                throw new RuntimeException("更新库存失败");
            }


        }


    }
}
