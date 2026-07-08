package com.gym.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeDTO {

    private String name;
    private String phone;
    private String password;
    private String role;
    private String position;
    private String department;
    private BigDecimal salary;
    private LocalDate hireDate;
}
