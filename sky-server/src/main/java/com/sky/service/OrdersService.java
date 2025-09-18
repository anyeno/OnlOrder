package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
public interface OrdersService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    OrderVO orderDetail(Long id);

    PageResult historyOrders(OrdersPageQueryDTO dto);

    void cancelOrder(Long id);

    void repeatOrder(Long id);

    OrderStatisticsVO statistics();

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    void adminCancelOrder(OrdersCancelDTO dto);

    void completeOrder(Long id);

    void rejectionOrder(OrdersRejectionDTO dto);

    void confirmOrder(Long id);

    void deliveryOrder(Long id);

    Boolean isAccessAble(OrdersSubmitDTO ordersSubmitDTO);
}
