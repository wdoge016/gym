package com.gym.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CoachDTO {

    private Long employeeId;
    private String name;
    private String phone;
    private Integer gender;
    private String speciality;
    private String description;
    private String avatarUrl;
    private String certification;
    private LocalDate hireDate;
}
