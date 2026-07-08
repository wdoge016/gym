package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("member_card")
public class MemberCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;
    private String cardType;
    private Integer totalTimes;
    private Integer remainingTimes;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
