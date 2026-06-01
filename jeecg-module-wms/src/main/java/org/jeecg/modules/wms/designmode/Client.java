package org.jeecg.modules.wms.designmode;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2025/9/7 17:29
 */
public class Client {

    //链头元素
    private Approver firstApprover;

    public Client(){
        // 创建处理者
        Approver projectManager = new ProjectManager();
        Approver departmentManager = new DepartmentManager();
        Approver generalManager = new GeneralManager();

        // 设置责任链
        projectManager.setNextApprover(departmentManager);
        departmentManager.setNextApprover(generalManager);
        this.firstApprover = projectManager;
    }



    /**
     * 处理方法
     * @param request
     */
    public void processRequest(PurchaseRequest request) {
        firstApprover.processRequest(request);
    }
    public static void main(String[] args) {
        Client client = new Client();
        PurchaseRequest request3 = new PurchaseRequest(3, 10000, "购买新设备");
        //调用client的方法进行处理
        client.processRequest(request3);

    }
//    public static void main(String[] args) {
//        // 创建处理者
//        Approver projectManager = new ProjectManager();
//        Approver departmentManager = new DepartmentManager();
//        Approver generalManager = new GeneralManager();
//
//        // 设置责任链
//        projectManager.setNextApprover(departmentManager);
//        departmentManager.setNextApprover(generalManager);
//
//        // 创建请求
//        PurchaseRequest request1 = new PurchaseRequest(1, 800, "购买办公用品");
//        PurchaseRequest request2 = new PurchaseRequest(2, 3500, "购买服务器");
//        PurchaseRequest request3 = new PurchaseRequest(3, 10000, "购买新设备");
//
//        // 处理请求
////        projectManager.processRequest(request1);
////        projectManager.processRequest(request2);
//        projectManager.processRequest(request3);
//    }
}
