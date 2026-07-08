package com.gym.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationVO {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long scheduleId;
    private String courseName;
    private String coachName;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String source;
    private String status;
    private LocalDateTime bookTime;
    private LocalDateTime checkInTime;
}
