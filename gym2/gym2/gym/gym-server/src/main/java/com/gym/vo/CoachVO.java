package com.gym.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CoachVO {

    private Long id;
    private Long employeeId;
    private String name;
    private String phone;
    private Integer gender;
    private String speciality;
    private String description;
    private String avatarUrl;
    private String certification;
    private Integer status;
    private LocalDate hireDate;
    private LocalDateTime createTime;
}
