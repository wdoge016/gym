package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.dto.LoginDTO;
import com.gym.entity.Employee;
import com.gym.exception.BusinessException;
import com.gym.mapper.EmployeeMapper;
import com.gym.security.JwtUtil;
import com.gym.service.EmployeeAuthService;
import com.gym.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAuthServiceImpl implements EmployeeAuthService {

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getPhone, dto.getUsername())
                .or()
                .eq(Employee::getName, dto.getUsername());
        Employee employee = employeeMapper.selectOne(wrapper);

        if (employee == null) {
            throw new BusinessException("账号或密码错误");
        }

        if (!passwordEncoder.matches(dto.getPassword(), employee.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        if (employee.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateEmployeeToken(employee.getId(), employee.getRole());

        log.info("员工登录成功: {} ({})", employee.getName(), employee.getRole());

        return LoginVO.builder()
                .userId(employee.getId())
                .username(employee.getName())
                .token(token)
                .userType(Constants.USER_TYPE_EMPLOYEE)
                .role(employee.getRole())
                .build();
    }
}
