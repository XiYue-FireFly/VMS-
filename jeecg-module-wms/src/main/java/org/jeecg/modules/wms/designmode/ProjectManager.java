package org.jeecg.modules.wms.designmode;

// 项目经理
public class ProjectManager extends Approver {
    @Override
    public void processRequest(PurchaseRequest request) {
        System.out.println("项目经理审批采购单：" + request.getId() +
                "，金额：" + request.getAmount() +
                "，用途：" + request.getPurpose());
        if (nextApprover != null) {
            nextApprover.processRequest(request);
        }
    }
}
