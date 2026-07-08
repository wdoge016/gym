package com.gym.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendVO {

    private String reason;
    private List<CourseItem> courses;

    @Data
    public static class CourseItem {
        private Long courseId;
        private String courseName;
        private String type;
        private Integer intensity;
        private BigDecimal price;
        private String coachName;
    }
}
