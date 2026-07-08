package com.gym.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardVO {

    private Long id;
    private Long memberId;
    private String memberName;
    private String cardType;
    private Integer totalTimes;
    private Integer remainingTimes;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
}
