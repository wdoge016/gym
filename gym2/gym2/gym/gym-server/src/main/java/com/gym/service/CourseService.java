package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.CourseDTO;
import com.gym.entity.Course;

import java.util.List;

public interface CourseService {

    IPage<Course> page(Integer current, Integer size, String keyword, String type);

    List<Course> listByType(String type);

    Course getById(Long id);

    void add(CourseDTO dto);

    void update(Long id, CourseDTO dto);

    void delete(Long id);
}
