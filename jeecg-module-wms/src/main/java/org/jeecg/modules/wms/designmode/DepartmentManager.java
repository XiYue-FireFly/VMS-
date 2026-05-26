package org.jeecg.modules.wms.designmode;

// 部门经理
public class DepartmentManager extends Approver {
    @Override
    public void processRequest(PurchaseRequest request) {
        if (request.getAmount() > 1000 ) {
            System.out.println("部门经理审批采购单：" + request.getId() +
                    "，金额：" + request.getAmount() +
                    "，用途：" + request.getPurpose());
        }
        if (nextApprover != null) {
            nextApprover.processRequest(request);
        }
    }
}
