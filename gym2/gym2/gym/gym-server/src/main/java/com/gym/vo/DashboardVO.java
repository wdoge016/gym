package com.gym.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {

    // 核心指标
    private BigDecimal monthIncome;
    private Long todayNewMembers;
    private Long todayBookings;
    private Long todaySchedules;

    // 辅助指标
    private Long totalMembers;
    private Long totalCoaches;
    private Long totalEquipment;
    private BigDecimal retentionRate;
    private BigDecimal occupancyRate;

    // 热门课程
    private List<HotCourse> hotCourses;

    @Data
    public static class HotCourse {
        private String courseName;
        private Integer bookingCount;
    }
}
