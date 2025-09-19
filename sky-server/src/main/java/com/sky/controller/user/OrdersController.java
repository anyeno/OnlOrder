package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrdersMapper;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/order")
@Api(tags = "c端-订单相关接口")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrdersMapper ordersMapper;


    @RequestMapping("/submit")
    public Result submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("order submit:{}", ordersSubmitDTO);
        Boolean isAccessAble = ordersService.isAccessAble(ordersSubmitDTO);
        if(!isAccessAble){
//            throw new OrderBusinessException("超出配送范围");
            return Result.error(MessageConstant.NOT_ACCESSABLE);
        }


        OrderSubmitVO res = ordersService.submit(ordersSubmitDTO);
        return Result.success(res);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = ordersService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result orderDetail(@PathVariable Long id) {
        log.info("查询订单详情:{}", id);
        OrderVO res = ordersService.orderDetail(id);
        return Result.success(res);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result historyOrders(OrdersPageQueryDTO dto) {
        log.info("查询历史订单: {}", dto);
        PageResult res = ordersService.historyOrders(dto);
        return Result.success(res);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("取消订单:{}", id);
        ordersService.cancelOrder(id);
        return Result.success();
    }

    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repeatOrder(@PathVariable Long id) {
        log.info("再来一单:{}", id);
        ordersService.repeatOrder(id);
        return Result.success();
    }

    @ApiOperation("催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        log.info("催单", id);
        ordersService.reminder(id);
        return Result.success();
    }
}
