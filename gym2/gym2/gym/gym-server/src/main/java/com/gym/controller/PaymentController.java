package com.gym.controller;

import com.gym.common.Result;
import com.gym.dto.PaymentDTO;
import com.gym.security.UserContext;
import com.gym.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "支付（模拟固定成功）")
    @PostMapping
    public Result<?> pay(@RequestBody PaymentDTO dto) {
        Long memberId = UserContext.getUserId();
        paymentService.pay(dto.getOrderId(), memberId, dto.getPayMethod());
        return Result.ok("支付成功");
    }

    @Operation(summary = "退款")
    @PostMapping("/{orderId}/refund")
    public Result<?> refund(@PathVariable Long orderId) {
        paymentService.refund(orderId);
        return Result.ok("退款成功");
    }
}
