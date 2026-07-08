package com.gym.service;

import com.gym.dto.LoginDTO;
import com.gym.dto.RegisterDTO;
import com.gym.vo.LoginVO;
import com.gym.vo.MemberVO;

public interface AuthService {

    /**
     * 会员注册
     */
    void register(RegisterDTO dto);

    /**
     * 会员登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 退出登录（加入Redis黑名单）
     */
    void logout(String token);

    /**
     * 获取当前登录会员信息
     */
    MemberVO getUserInfo(Long memberId);

    /**
     * 修改密码
     */
    void changePassword(Long memberId, String oldPassword, String newPassword);
}
