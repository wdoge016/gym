package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.Constants;
import com.gym.entity.Order;
import com.gym.exception.BusinessException;
import com.gym.mapper.OrderMapper;
import com.gym.service.OrderService;
import com.gym.utils.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public Order create(Long memberId, String orderType, BigDecimal amount, String remark) {
        Order order = new Order();
        order.setOrderNo(SnowflakeIdUtil.nextIdStr());
        order.setMemberId(memberId);
        order.setOrderType(orderType);
        order.setAmount(amount);
        order.setStatus(Constants.STATUS_PENDING);
        order.setRemark(remark);
        orderMapper.insert(order);
        return order;
    }

    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public IPage<Order> getMyOrders(Long memberId, Integer current, Integer size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMemberId, memberId)
                .orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public IPage<Order> pageAll(Integer current, Integer size, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public void cancel(Long orderId) {
        Order order = getById(orderId);
        if (!Constants.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("仅可取消待支付订单");
        }
        order.setStatus(Constants.STATUS_CANCELLED);
        orderMapper.updateById(order);
    }

    /**
     * 更新订单状态
     */
    public void updateStatus(Long orderId, String status) {
        Order order = getById(orderId);
        order.setStatus(status);
        if (Constants.STATUS_PAID.equals(status)) {
            order.setPayTime(LocalDateTime.now());
        }
        orderMapper.updateById(order);
    }
}
