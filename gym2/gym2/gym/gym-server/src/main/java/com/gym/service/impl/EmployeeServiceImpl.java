package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.dto.EmployeeDTO;
import com.gym.entity.Coach;
import com.gym.entity.Employee;
import com.gym.exception.BusinessException;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.EmployeeMapper;
import com.gym.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final CoachMapper coachMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public IPage<Employee> page(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Employee::getName, keyword)
                    .or()
                    .like(Employee::getPhone, keyword);
        }
        wrapper.orderByDesc(Employee::getCreateTime);
        return employeeMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    @Override
    public void add(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employee.setRole(dto.getRole() != null ? dto.getRole() : "STAFF");
        employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(1);
        employeeMapper.insert(employee);
    }

    @Override
    public void update(Long id, EmployeeDTO dto) {
        Employee employee = getById(id);
        if (StringUtils.hasText(dto.getName())) employee.setName(dto.getName());
        if (StringUtils.hasText(dto.getPhone())) employee.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getPassword())) employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (StringUtils.hasText(dto.getRole())) employee.setRole(dto.getRole());
        if (StringUtils.hasText(dto.getPosition())) employee.setPosition(dto.getPosition());
        if (StringUtils.hasText(dto.getDepartment())) employee.setDepartment(dto.getDepartment());
        if (dto.getSalary() != null) employee.setSalary(dto.getSalary());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        employeeMapper.updateById(employee);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Employee employee = getById(id);
        employee.setStatus(status);
        employeeMapper.updateById(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 级联删除关联的教练记录
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getEmployeeId, id);
        Coach coach = coachMapper.selectOne(wrapper);
        if (coach != null) {
            coachMapper.deleteById(coach.getId());
        }
        employeeMapper.deleteById(id);
    }
}
