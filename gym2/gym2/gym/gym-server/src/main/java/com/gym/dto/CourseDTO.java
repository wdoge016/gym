package com.gym.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDTO {

    private String name;
    private String description;
    private String type;
    private Integer intensity;
    private Integer duration;
    private Integer maxCapacity;
    private Long coachId;
    private BigDecimal price;
    private String coverImage;
    private Integer status;
}
