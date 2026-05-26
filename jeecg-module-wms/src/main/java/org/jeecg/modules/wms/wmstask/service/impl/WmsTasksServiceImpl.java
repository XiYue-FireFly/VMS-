package org.jeecg.modules.wms.wmstask.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date:   2025-11-23
 * @Version: V1.0
 */
@Service
public class WmsTasksServiceImpl extends ServiceImpl<WmsTasksMapper, WmsTasks> implements IWmsTasksService {

    @Autowired
    private IWmsStockInOrdersService wmsStockInOrdersService;

    @Autowired
    private IWmsStockInOrderItemsService wmsStockInOrderItemsService;

    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReceiveTask(String orderId, String operator) {
        //查询入库单
        WmsStockInOrders wmsStockInOrders = wmsStockInOrdersService.getById(orderId);

        //校验入库单的状态为审核通过方可创建收货任务
        if(!wmsStockInOrders.getStatus().equals(WarehouseDictEnum.INBOUND_APPROVED.getCode())){
            throw new RuntimeException("只能审核通过的入库单方可创建收货！");
        }

        //查询入库单明细
        List<WmsStockInOrderItems> wmsStockInOrderItems = wmsStockInOrderItemsService.selectByMainId(orderId);

        //遍历入库单明细,创建收货任务
        for (WmsStockInOrderItems entity : wmsStockInOrderItems){
            //创建收货任务
            WmsTasks wmsTasks = new WmsTasks();
            wmsTasks.setTaskNumber(generateTaskCode());//任务编号
            //任务类型
            wmsTasks.setTaskType(WarehouseDictEnum.TASK_TYPE_RECEIVING.getCode());
            //任务状态
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_CREATED.getCode());
            //商品id
            wmsTasks.setProductId(entity.getProductId());
            // 数量
            wmsTasks.setQuantity(entity.getExpectedQuantity());
            //完成数量为0
            wmsTasks.setCompletedQuantity(0);
            //执行人
            wmsTasks.setOperator(operator);
            //入库单id
            wmsTasks.setStockInOrderId(orderId);
            //入库单明细id
            wmsTasks.setStockInOrderItemId(entity.getId());
            //目的仓库
            wmsTasks.setTargetWarehouseId(wmsStockInOrders.getWarehouseId());
            boolean save = this.save(wmsTasks);
            if(!save){
                throw new RuntimeException("创建收货任务失败！");
            }

        }

        //更新入库单的状态为收货中
        WmsStockInOrders stockInOrdersUpdate = new WmsStockInOrders();
        stockInOrdersUpdate.setId(orderId);
        stockInOrdersUpdate.setStatus(WarehouseDictEnum.INBOUND_RECEIVING.getCode());
        wmsStockInOrdersService.updateById(stockInOrdersUpdate);

        //更新入库单明细的状态为收货中
        //sql update wms_stock_in_order_items set status = 'RECEIVING' where order_id = #{orderId}
        LambdaUpdateWrapper<WmsStockInOrderItems> set = new LambdaUpdateWrapper<WmsStockInOrderItems>()
                .eq(WmsStockInOrderItems::getOrderId, orderId)
                .set(WmsStockInOrderItems::getStatus, WarehouseDictEnum.INBOUND_DETAIL_RECEIVING.getCode());
        wmsStockInOrderItemsService.update(set);


    }

    @Override
    public IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize) {
        Page<WmsTasks> pageResult = PageHelper.startPage(pageNo, pageSize);
        //调用mapper
        List<WmsTasks> list = baseMapper.queryTaskList(wmsTasks);
        PageDTO<WmsTasks> wmsTasksPageDTO = new PageDTO<>();
        wmsTasksPageDTO.setRecords(list);
        wmsTasksPageDTO.setTotal(pageResult.getTotal());
        wmsTasksPageDTO.setSize(pageSize);
        wmsTasksPageDTO.setCurrent(pageNo);
        wmsTasksPageDTO.setPages(pageResult.getPages());
        return wmsTasksPageDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(WmsTasksRecords wmsTasksRecords) {
        //执行任务 添加任务记录 在任务表记录完成数量 更新任务状态 此方法是一个公共方法用于上架、收货、拣货
        WmsTasks wmsTasks = execute(wmsTasksRecords);
        //入库单明细id
        String stockInOrderItemId = wmsTasks.getStockInOrderItemId();

        //更新入库单明细的收货数量、不良品数据及状态
        wmsStockInOrderItemsService.updateReceivedStatus(stockInOrderItemId);

        //更新入库单中的总收货数量、总不良品数据、状态
        wmsStockInOrdersService.updateReceivedStatus(wmsTasks.getStockInOrderId());


        //todo 如果入库单收货完成自动创建上架任务

        //todo 存储库存

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public  WmsTasks execute(WmsTasksRecords wmsTasksRecords) {
        //任务id
        String taskId = wmsTasksRecords.getTaskId();
        //查询任务
        WmsTasks wmsTasks = this.getById(taskId);
        if(wmsTasks == null){
            throw new RuntimeException("任务不存在！");
        }
        //计划数量
        Integer planQuantity = wmsTasks.getQuantity();
        //已完成的数据
        Integer completedQuantity = wmsTasks.getCompletedQuantity();
        //本次执行数量
        Integer executeQuantity = wmsTasksRecords.getExecQuantity();
        //如果完成数量加上本次执行数量大于计划数量则不能执行
        if(completedQuantity + executeQuantity > planQuantity){
            throw new RuntimeException("本次执行数量不能大于计划数量！");
        }

        //向任务记录表填充数据
        //执行人
        wmsTasksRecords.setOperator(wmsTasks.getOperator());
        //执行时间为当前时间
        wmsTasksRecords.setOperationTime(new Date());
        //入库单id
        wmsTasksRecords.setStockInOrderId(wmsTasks.getStockInOrderId());
        //入库单明细id
        wmsTasksRecords.setStockInOrderItemId(wmsTasks.getStockInOrderItemId());
        //波次单id
        wmsTasksRecords.setWaveOrderId(wmsTasks.getWaveOrderId());
        //任务id
        wmsTasksRecords.setTaskId(taskId);
        //出库单id
        wmsTasksRecords.setOutOrderId(wmsTasks.getOutOrderId());
        //波次拣货明细id
        wmsTasksRecords.setWaveSkuSummaryId(wmsTasks.getWaveSkuSummaryId());
        //来源仓库
        wmsTasksRecords.setSourceWarehouseId(wmsTasks.getSourceWarehouseId());
        //目的仓库
        wmsTasksRecords.setTargetWarehouseId(wmsTasks.getTargetWarehouseId());
        boolean save = wmsTasksRecordsService.save(wmsTasksRecords);
        if(!save){
            throw new RuntimeException("保存任务记录失败！");
        }

        //向任务表增加完成收货数量
        //sql update wms_tasks set completed_quantity = completed_quantity + #{executeQuantity} where id = #{taskId}
        //                                    and completed_quantity <= 计划数量-#{executeQuantity}
        LambdaUpdateWrapper<WmsTasks> wmsTasksLambdaUpdateWrapper = new LambdaUpdateWrapper<WmsTasks>()
                .eq(WmsTasks::getId, taskId)
                .setSql("completed_quantity = completed_quantity + " + executeQuantity)
                .le(WmsTasks::getCompletedQuantity, planQuantity-executeQuantity);

        boolean update = this.update(wmsTasksLambdaUpdateWrapper);
        if(!update){
            throw new RuntimeException("更新任务失败！");
        }
        //重新查询最新数据
        wmsTasks = this.getById(taskId);
        //最新完成数据
        completedQuantity = wmsTasks.getCompletedQuantity();
        if(completedQuantity >= planQuantity){
            //更新任务状态为收货完成
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
            this.updateById(wmsTasks);
        }

        return wmsTasks;
    }

    /**
     * 生成任务编号
     * 规则: TSK+年月日+5位序号，序号使用redis自增序号实现
     */
    public String generateTaskCode() {
        //参考上边的代码实现
        String time = DateUtils.now().substring(0, 10).replace("-", "");
        String key = "tsk_number"+time;
        long incr = redisUtil.incr(key, 1);
        if(incr == 1){
            //设置过期时间，设置24小时+10秒的目的是避免并发产生订单号重复
            redisUtil.expire(key, 24*60*60+10);
        }
        //将incr组成4位字符串
        String incrStr = String.format("%05d", incr);
        String taskNumber = "TSK"+time+incrStr;

        return taskNumber;
    }
}
