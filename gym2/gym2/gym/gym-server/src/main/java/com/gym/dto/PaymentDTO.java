package com.gym.dto;

import lombok.Data;

@Data
public class PaymentDTO {

    private Long orderId;
    private String payMethod;
}
