package org.jeecg.modules.wms.designmode2;

// 首单折扣策略
class FirstOrderDiscount implements DiscountStrategy {
    @Override
    public boolean apply(Order order) {
        if (order.isFirstOrder()) {
            double newAmount = order.getAmount() * 0.8; // 首单8折
            order.setAmount(newAmount);
            System.out.println("应用首单折扣，新金额: " + newAmount);
            return true;
        }
        return false;
    }
}
