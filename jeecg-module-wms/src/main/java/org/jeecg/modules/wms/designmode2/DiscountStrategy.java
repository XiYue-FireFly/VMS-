package org.jeecg.modules.wms.designmode2;

// 折扣策略接口
interface DiscountStrategy {
    boolean apply(Order order);
}
