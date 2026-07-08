package com.gym.service;

import com.gym.dto.LoginDTO;
import com.gym.vo.LoginVO;

public interface EmployeeAuthService {

    /**
     * 员工后台登录
     */
    LoginVO login(LoginDTO dto);
}
