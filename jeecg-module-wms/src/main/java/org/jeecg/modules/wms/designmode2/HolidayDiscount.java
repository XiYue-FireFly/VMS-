package org.jeecg.modules.wms.designmode2;

// 节日折扣策略
class HolidayDiscount implements DiscountStrategy {
    @Override
    public boolean apply(Order order) {
        if (order.isHoliday()) {
            double newAmount = order.getAmount() * 0.85; // 节日85折
            order.setAmount(newAmount);
            System.out.println("应用节日折扣，新金额: " + newAmount);
            return true;
        }
        return false;
    }
}
