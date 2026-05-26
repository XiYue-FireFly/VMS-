package org.jeecg.modules.wms.designmode2;

// 会员折扣策略
class MemberDiscount implements DiscountStrategy {
    @Override
    public boolean apply(Order order) {
        if (order.isMember()) {
            double newAmount = order.getAmount() * 0.9; // 会员9折
            order.setAmount(newAmount);
            System.out.println("应用会员折扣，新金额: " + newAmount);
            return true;
        }
        return false;
    }
}
