package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.CoachDTO;
import com.gym.entity.Coach;
import com.gym.entity.Employee;
import com.gym.exception.BusinessException;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.EmployeeMapper;
import com.gym.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachMapper coachMapper;
    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public IPage<Coach> page(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Coach::getName, keyword)
                    .or()
                    .like(Coach::getSpeciality, keyword);
        }
        wrapper.orderByDesc(Coach::getCreateTime);
        return coachMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public List<Coach> listAll() {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getStatus, 1);
        return coachMapper.selectList(wrapper);
    }

    @Override
    public Coach getById(Long id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }
        return coach;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CoachDTO dto) {
        // 1. 先创建员工记录（教练也是员工）
        Employee emp = new Employee();
        emp.setName(dto.getName());
        emp.setPhone(dto.getPhone());
        emp.setPassword(passwordEncoder.encode("123456")); // 默认密码
        emp.setRole("COACH");
        emp.setPosition("教练");
        emp.setDepartment("教练部");
        emp.setHireDate(dto.getHireDate());
        emp.setStatus(1);
        employeeMapper.insert(emp);

        // 2. 创建教练记录，关联员工ID
        Coach coach = new Coach();
        coach.setEmployeeId(emp.getId());
        coach.setName(dto.getName());
        coach.setPhone(dto.getPhone());
        coach.setGender(dto.getGender());
        coach.setSpeciality(dto.getSpeciality());
        coach.setDescription(dto.getDescription());
        coach.setAvatarUrl(dto.getAvatarUrl());
        coach.setCertification(dto.getCertification());
        coach.setHireDate(dto.getHireDate());
        coach.setStatus(1);
        coachMapper.insert(coach);
    }

    @Override
    public void update(Long id, CoachDTO dto) {
        Coach coach = getById(id);
        if (StringUtils.hasText(dto.getName())) coach.setName(dto.getName());
        if (StringUtils.hasText(dto.getPhone())) coach.setPhone(dto.getPhone());
        if (dto.getGender() != null) coach.setGender(dto.getGender());
        if (StringUtils.hasText(dto.getSpeciality())) coach.setSpeciality(dto.getSpeciality());
        if (StringUtils.hasText(dto.getDescription())) coach.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getAvatarUrl())) coach.setAvatarUrl(dto.getAvatarUrl());
        if (StringUtils.hasText(dto.getCertification())) coach.setCertification(dto.getCertification());
        if (dto.getHireDate() != null) coach.setHireDate(dto.getHireDate());
        coachMapper.updateById(coach);
    }

    @Override
    public void delete(Long id) {
        coachMapper.deleteById(id);
    }
}
