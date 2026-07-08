package com.gym.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDTO {

    private String orderType;
    private BigDecimal amount;
    private String remark;
}
