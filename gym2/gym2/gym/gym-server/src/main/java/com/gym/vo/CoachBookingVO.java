package com.gym.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CoachBookingVO {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long coachId;
    private String coachName;
    private String coachSpeciality;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String remark;
}
