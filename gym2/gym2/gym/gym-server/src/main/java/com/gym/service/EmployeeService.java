package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.EmployeeDTO;
import com.gym.entity.Employee;

public interface EmployeeService {

    IPage<Employee> page(Integer current, Integer size, String keyword);

    Employee getById(Long id);

    void add(EmployeeDTO dto);

    void update(Long id, EmployeeDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
