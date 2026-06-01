package org.jeecg.modules.wms.designmode2;

// 客户端代码
public class OrderProcessingDemo {
    public static void main(String[] args) {
        // 创建策略链
        DiscountStrategyChain chain = new DiscountStrategyChain();
        chain.addStrategy(new MemberDiscount())
             .addStrategy(new HolidayDiscount())
             .addStrategy(new FirstOrderDiscount())
             .addStrategy(new BigOrderDiscount());

        // 处理订单1: 会员 + 节日
        System.out.println("\n处理订单1:");
        Order order1 = new Order(500, true, true, false);
        chain.applyDiscounts(order1);
        System.out.println("最终金额: " + order1.getAmount());

        // 处理订单2: 首单 + 大额
        System.out.println("\n处理订单2:");
        Order order2 = new Order(1500, false, false, true);
        chain.applyDiscounts(order2);
        System.out.println("最终金额: " + order2.getAmount());

        // 处理订单3: 无折扣
        System.out.println("\n处理订单3:");
        Order order3 = new Order(200, false, false, false);
        chain.applyDiscounts(order3);
        System.out.println("最终金额: " + order3.getAmount());
    }
}
