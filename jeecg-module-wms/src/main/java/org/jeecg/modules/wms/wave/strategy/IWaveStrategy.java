package org.jeecg.modules.wms.wave.strategy;

import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 抽象策略类
 * @date 2025/11/29 16:44
 */
public abstract class IWaveStrategy {

    /**
     * 下一个策略
     */
    protected IWaveStrategy nextStrategy;

    /**
     * 设置下一个策略
     */
    public void setNextStrategy(IWaveStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }


    /**
     * 匹配订单，创建波次
     *
     * @param orders         订单列表
     * @param allocationsMap 订单分配列表
     */
    abstract void process(List<WmsOutOrders> orders,
                          Map<String, List<WmsOutOrdersAllocation>> allocationsMap);


    /**
     * 执行下一个策略
     */
    protected void processNext(List<WmsOutOrders> orders,
                               Map<String, List<WmsOutOrdersAllocation>> allocationsMap){
        //如果orders不为空并且下一个策略不为空，则执行下一个策略
        if (orders != null && orders.size()>0 && nextStrategy != null) {
            nextStrategy.process(orders, allocationsMap);
        }

    }

    /**
     * 获取策略类型
     */
    abstract String getStrategyType();

    /**
     * 获取策略优先级(数值越小优先级越高)
     */
    abstract int getPriority();

    /**
     * 下一个策略
     */
    public IWaveStrategy  next(){
        return nextStrategy;
    }
}
