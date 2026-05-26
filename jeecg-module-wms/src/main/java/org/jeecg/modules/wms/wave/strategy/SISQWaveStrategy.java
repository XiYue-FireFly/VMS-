package org.jeecg.modules.wms.wave.strategy;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersItemsService;
import org.jeecg.modules.wms.wave.entity.WmsWaveStrategy;
import org.jeecg.modules.wms.wave.service.IWaveStrategyService;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mr.M
 * @version 1.0
 * @description 单品单件波次策略类
 * @date 2025/11/29 16:55
 */
@Component
@RequiredArgsConstructor
public class SISQWaveStrategy extends IWaveStrategy {

    private final IWmsWaveMasterService wmsWaveMasterService;

    private final IWaveStrategyService waveStrategyService;

    private final IWmsOutOrdersItemsService wmsOutOrdersItemsService;

    @Override
    void process(List<WmsOutOrders> orders, Map<String, List<WmsOutOrdersAllocation>> allocationsMap) {

        //包裹策略为STANDARD_STRATEGY
        String shipmentStrategy = "STANDARD_STRATEGY";

        //筛选满足单品单件的订单
        List<WmsOutOrders> collect = orders.stream().filter(order -> order.getTotalSku() == 1 && order.getTotalQuantity() == 1).collect(Collectors.toList());
        //如果满足单品单件的订单为空，则返回
        if (collect.isEmpty()) {
            return;
        }
        //从波次策略表查询最小订单数
        WmsWaveStrategy strategyByCode = waveStrategyService.getStrategyByCode(getStrategyType());
        int minOrderCount = strategyByCode.getMinOrderCount();
        //从collect提取出库单id
        List<String> outOrderIds = collect.stream().map(WmsOutOrders::getId).collect(Collectors.toList());

        //根据出库单id找出库单明细
        List<WmsOutOrdersItems> wmsOutOrdersItems = wmsOutOrdersItemsService.selectByOrderIds(outOrderIds);

        //将wmsOutOrdersItems中的skuid赋值到collect中
        collect.forEach(order -> {
            //找到和出库单匹配的出库单明细，从而找到该明细中的skuid
            WmsOutOrdersItems wmsOutOrdersItems1 = wmsOutOrdersItems.stream().filter(item -> item.getOrderId().equals(order.getId())).findFirst().orElse(null);
            order.setSkuId(wmsOutOrdersItems1.getSkuId());
        });
        //定义一个List，存放创建波次的订单
        List<WmsOutOrders> waveOrders = new ArrayList<>();
        //对collect订单按商品进行分组
        Map<String, List<WmsOutOrders>> collect1 = collect.stream().collect(Collectors.groupingBy(WmsOutOrders::getSkuId));
        //遍历<商品id,List<订单>>
        for (Map.Entry<String, List<WmsOutOrders>> entry : collect1.entrySet()) {
            //商品id
            String skuId = entry.getKey();
            //该商品的出库单列表
            List<WmsOutOrders> value = entry.getValue();
            //如果达到最小订单数据才创建波次
            if (value.size() >= minOrderCount) {
                //创建波次
                wmsWaveMasterService.addWave(value, getStrategyType(), shipmentStrategy);
                //放入waveOrders
                waveOrders.addAll( value);

            }

        }
        //拿到剩余订单,orders中不包含waveOrders
        List<WmsOutOrders> remainingOrders = orders.stream().filter(order -> !waveOrders.contains(order)).collect(Collectors.toList());
        //执行下一个
        processNext(remainingOrders, allocationsMap);

    }

    @Override
    String getStrategyType() {
        return "SISQ";
    }

    @Override
    int getPriority() {
        return 1;
    }
}
