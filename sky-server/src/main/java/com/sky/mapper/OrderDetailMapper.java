package com.sky.mapper;

import com.sky.entity.OrderDetail;

import java.util.List;

public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetails);
}
