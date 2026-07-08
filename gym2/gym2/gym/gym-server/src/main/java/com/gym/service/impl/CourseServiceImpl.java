package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.CourseDTO;
import com.gym.entity.Course;
import com.gym.exception.BusinessException;
import com.gym.mapper.CourseMapper;
import com.gym.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public IPage<Course> page(Integer current, Integer size, String keyword, String type) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getName, keyword);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Course::getType, type);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return courseMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public List<Course> listByType(String type) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Course::getType, type);
        }
        wrapper.orderByAsc(Course::getIntensity);
        return courseMapper.selectList(wrapper);
    }

    @Override
    public Course getById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    @Override
    public void add(CourseDTO dto) {
        Course course = new Course();
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setType(dto.getType());
        course.setIntensity(dto.getIntensity());
        course.setDuration(dto.getDuration());
        course.setMaxCapacity(dto.getMaxCapacity());
        course.setCoachId(dto.getCoachId());
        course.setPrice(dto.getPrice());
        course.setCoverImage(dto.getCoverImage());
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        courseMapper.insert(course);
    }

    @Override
    public void update(Long id, CourseDTO dto) {
        Course course = getById(id);
        if (StringUtils.hasText(dto.getName())) course.setName(dto.getName());
        if (StringUtils.hasText(dto.getDescription())) course.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getType())) course.setType(dto.getType());
        if (dto.getIntensity() != null) course.setIntensity(dto.getIntensity());
        if (dto.getDuration() != null) course.setDuration(dto.getDuration());
        if (dto.getMaxCapacity() != null) course.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getCoachId() != null) course.setCoachId(dto.getCoachId());
        if (dto.getPrice() != null) course.setPrice(dto.getPrice());
        if (StringUtils.hasText(dto.getCoverImage())) course.setCoverImage(dto.getCoverImage());
        if (dto.getStatus() != null) course.setStatus(dto.getStatus());
        courseMapper.updateById(course);
    }

    @Override
    public void delete(Long id) {
        courseMapper.deleteById(id);
    }
}
