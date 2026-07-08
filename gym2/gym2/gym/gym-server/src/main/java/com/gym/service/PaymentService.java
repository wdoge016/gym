package com.gym.service;

public interface PaymentService {

    /**
     * 模拟支付（固定成功）
     */
    void pay(Long orderId, Long memberId, String payMethod);

    /**
     * 退款
     */
    void refund(Long orderId);
}
