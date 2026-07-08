package com.gym.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleVO {

    private Long id;
    private Long courseId;
    private String courseName;
    private String courseType;
    private Integer intensity;
    private Long coachId;
    private String coachName;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxMembers;
    private Integer bookedCount;
    private Integer cancelDeadline;
    private Integer status;
    private String remark;
}
