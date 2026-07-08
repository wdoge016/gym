package com.gym.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Integer gender;
    private Integer creditScore;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
