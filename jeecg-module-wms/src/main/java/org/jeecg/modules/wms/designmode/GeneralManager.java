package org.jeecg.modules.wms.designmode;

// 总经理
public class GeneralManager extends Approver {
    @Override
    public void processRequest(PurchaseRequest request) {
        if(request.getAmount() > 5000){
            System.out.println("总经理审批采购单：" + request.getId() +
                    "，金额：" + request.getAmount() +
                    "，用途：" + request.getPurpose());
        }
    }
}
