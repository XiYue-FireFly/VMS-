package org.jeecg.modules.wms.designmode;

// 抽象处理者
public abstract class Approver {
    protected Approver nextApprover;  // 下一个处理者

    // 设置下一个处理者
    public void setNextApprover(Approver nextApprover) {
        this.nextApprover = nextApprover;
    }

    // 处理请求的方法
    public abstract void processRequest(PurchaseRequest request);
}
