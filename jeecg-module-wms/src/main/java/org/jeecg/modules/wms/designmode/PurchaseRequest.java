package org.jeecg.modules.wms.designmode;

import lombok.Data;

// 请求类
@Data
public class PurchaseRequest {
    private int id;
    private double amount;
    private String purpose;

    public PurchaseRequest(int id, double amount, String purpose) {
        this.id = id;
        this.amount = amount;
        this.purpose = purpose;
    }
}
