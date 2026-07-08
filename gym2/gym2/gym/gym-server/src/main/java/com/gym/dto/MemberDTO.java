package com.gym.dto;

import lombok.Data;

@Data
public class MemberDTO {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer gender;
    private Integer creditScore;
}
