package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端-订单相关接口")
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    public Result statistics() {
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO res = ordersService.statistics();
        return Result.success(res);
    }


    @GetMapping("/conditionSearch")
    public Result conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索");
        PageResult res = ordersService.conditionSearch(ordersPageQueryDTO);
        return Result.success(res);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result orderDetail(@PathVariable Long id) {
        log.info("管理端-查询订单详情:{}", id);
        OrderVO res = ordersService.orderDetail(id);
        return Result.success(res);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO dto) {
        log.info("管理端-取消订单:{}");
        ordersService.adminCancelOrder(dto);
        return Result.success();
    }

    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        log.info("管理端-完成订单:{}", id);
        ordersService.completeOrder(id);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result rejectionOrder(@RequestBody OrdersRejectionDTO dto) {
        log.info("管理端-拒单:{}", dto.getId());
        ordersService.rejectionOrder(dto);
        return Result.success();
    }

    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO confirmDTO) {
        log.info("管理端-拒单:{}", confirmDTO.getId());
        ordersService.confirmOrder(confirmDTO.getId());
        return Result.success();
    }

    @ApiOperation("接单")
    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Long id) {
        log.info("管理端-派送订单:{}", id);
        ordersService.deliveryOrder(id);
        return Result.success();
    }
}
