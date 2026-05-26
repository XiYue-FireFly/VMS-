package org.jeecg.modules.wms.wave.strategy;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mr.M
 * @version 1.0
 * @description 执行责任链模式的客户类
 * @date 2025/11/29 17:27
 */

public class WaveCreateClient {

    /**
     * 定义一个链头策略对象
     */
    private IWaveStrategy firstStrategy;

    /**
     * 在构造方法中将所有的策略对象组成一个链
     * @param strategies 所有策略对象
     *  @param  selectedStrategies 页面传入的
     */
    public WaveCreateClient(List<IWaveStrategy> strategies, List<String> selectedStrategies) {
        //从strategies所有策略对象中找到selectedStrategies的策略类，并且按优先级升序排序
        List<IWaveStrategy> selectedStrategiesList = strategies.stream()
                .filter(strategy -> selectedStrategies.contains(strategy.getStrategyType()))
                .sorted((s1, s2) -> Integer.compare(s1.getPriority(), s2.getPriority()))//按优先级升序排序
                .collect(Collectors.toList());

        //链头元素
       this.firstStrategy = selectedStrategiesList.get(0);

        //将selectedStrategiesList组成链
        for (int i = 1; i < selectedStrategiesList.size(); i++) {
            IWaveStrategy currentStrategy = selectedStrategiesList.get(i-1);//拿到上一个策略对象
             currentStrategy.setNextStrategy(selectedStrategiesList.get(i));//将下一个策略对象设置给上一个策略对象
        }

    }
    /**
     * 处理订单波次分配
     */
    public void process(List<WmsOutOrders> orders,
                        Map<String, List<WmsOutOrdersAllocation>> allocationsMap) {
        if(firstStrategy == null){
            throw new JeecgBootException("没有配置波次策略");
        }
        firstStrategy.process(orders, allocationsMap);
    }

}
