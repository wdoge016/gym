package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.Constants;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.OrderDTO;
import com.gym.entity.Order;
import com.gym.security.UserContext;
import com.gym.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> create(@RequestBody OrderDTO dto) {
        Long memberId = UserContext.getUserId();
        return Result.ok(orderService.create(memberId, dto.getOrderType(), dto.getAmount(), dto.getRemark()));
    }

    @Operation(summary = "我的订单")
    @GetMapping("/my")
    public Result<PageResult<Order>> myOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = UserContext.getUserId();
        IPage<Order> page = orderService.getMyOrders(memberId, current, size);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok("取消成功");
    }

    @Operation(summary = "全部订单（管理端）")
    @GetMapping
    public Result<PageResult<Order>> listAll(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        IPage<Order> page = orderService.pageAll(current, size, status);
        return Result.ok(PageResult.of(page));
    }
}
