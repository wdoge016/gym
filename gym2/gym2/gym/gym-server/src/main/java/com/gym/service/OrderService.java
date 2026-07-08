package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.entity.Order;

public interface OrderService {

    Order create(Long memberId, String orderType, java.math.BigDecimal amount, String remark);

    Order getById(Long id);

    IPage<Order> getMyOrders(Long memberId, Integer current, Integer size);

    IPage<Order> pageAll(Integer current, Integer size, String status);

    void cancel(Long orderId);
}
