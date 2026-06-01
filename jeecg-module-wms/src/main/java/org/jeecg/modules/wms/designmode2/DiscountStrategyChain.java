package org.jeecg.modules.wms.designmode2;

import java.util.ArrayList;
import java.util.List;

// 折扣策略链
class DiscountStrategyChain {
    private List<DiscountStrategy> strategies = new ArrayList<>();

    public DiscountStrategyChain addStrategy(DiscountStrategy strategy) {
        strategies.add(strategy);
        return this;
    }

    public void applyDiscounts(Order order) {
        for (DiscountStrategy strategy : strategies) {
            strategy.apply(order);
        }
    }
}
