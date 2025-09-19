package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;
    /**
     * 每分钟检查是否有超时订单
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void processOutTimeOrder() {
        log.info("执行定时任务 检查订单是否超时");
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = ordersMapper.selectBYStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        if(ordersList!=null && ordersList.size() > 0) {
            ordersList.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时，自动取消");
                ordersMapper.update(order);
            });
        }
    }

    /**
     * 凌晨四点检查是否存在忘记点完成导致一直派送中的订单
     */
    @Scheduled(cron = "0 0 4 * * ?")
//    @Scheduled(cron = "0 55 13 * * ?")  // 测试用
    public void processDeliveryOrder() {
        log.info("执行定时任务 检查订单是否一直处于派送中");
        LocalDateTime time = LocalDateTime.now().minusHours(2);
        List<Orders> ordersList = ordersMapper.selectBYStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList!=null && ordersList.size() > 0) {
            ordersList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                order.setDeliveryTime(time);
                ordersMapper.update(order);
            });
        }
    }

}
