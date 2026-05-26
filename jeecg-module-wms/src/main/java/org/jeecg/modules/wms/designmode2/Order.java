package org.jeecg.modules.wms.designmode2;

import lombok.Data;

// 订单类
@Data
class Order {
    private double amount;
    private boolean isMember;
    private boolean isHoliday;
    private boolean isFirstOrder;

    public Order(double amount, boolean isMember, boolean isHoliday, boolean isFirstOrder) {
        this.amount = amount;
        this.isMember = isMember;
        this.isHoliday = isHoliday;
        this.isFirstOrder = isFirstOrder;
    }
}
