package org.jeecg.modules.wms.designmode2;

// 大额订单折扣策略
class BigOrderDiscount implements DiscountStrategy {
    @Override
    public boolean apply(Order order) {
        if (order.getAmount() > 1000) {
            double newAmount = order.getAmount() * 0.7; // 大额7折
            order.setAmount(newAmount);
            System.out.println("应用大额折扣，新金额: " + newAmount);
            return true;
        }
        return false;
    }
}
