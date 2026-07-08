package com.gym.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {

    private Long id;
    private String name;
    private String description;
    private String type;
    private Integer intensity;
    private Integer duration;
    private Integer maxCapacity;
    private Long coachId;
    private String coachName;
    private BigDecimal price;
    private String coverImage;
    private Integer status;
    private LocalDateTime createTime;
}
