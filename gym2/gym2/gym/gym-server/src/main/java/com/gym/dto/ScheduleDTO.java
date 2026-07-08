package com.gym.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleDTO {

    private Long courseId;
    private Long coachId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxMembers;
    private Integer cancelDeadline;
    private String remark;
}
