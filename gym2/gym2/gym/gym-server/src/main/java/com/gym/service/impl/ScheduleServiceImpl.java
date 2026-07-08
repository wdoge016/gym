package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.ScheduleDTO;
import com.gym.entity.Coach;
import com.gym.entity.Course;
import com.gym.entity.Schedule;
import com.gym.exception.BusinessException;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.CourseMapper;
import com.gym.mapper.ScheduleMapper;
import com.gym.service.ScheduleService;
import com.gym.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;

    @Override
    public IPage<ScheduleVO> page(Integer current, Integer size, Long courseId, LocalDate date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Schedule::getCourseId, courseId);
        }
        if (date != null) {
            wrapper.between(Schedule::getStartTime,
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX));
        }
        wrapper.orderByAsc(Schedule::getStartTime);

        IPage<Schedule> page = scheduleMapper.selectPage(new Page<>(current, size), wrapper);
        return page.convert(this::toVO);
    }

    @Override
    public List<ScheduleVO> listAvailable() {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getStatus, 1)
                .gt(Schedule::getStartTime, LocalDateTime.now())
                .apply("booked_count < max_members")
                .orderByAsc(Schedule::getStartTime);
        return scheduleMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleVO getById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }
        return toVO(schedule);
    }

    @Override
    public void add(ScheduleDTO dto) {
        // 检查课程和教练是否存在
        if (courseMapper.selectById(dto.getCourseId()) == null) {
            throw new BusinessException("课程不存在");
        }
        if (coachMapper.selectById(dto.getCoachId()) == null) {
            throw new BusinessException("教练不存在");
        }

        Schedule schedule = new Schedule();
        schedule.setCourseId(dto.getCourseId());
        schedule.setCoachId(dto.getCoachId());
        schedule.setRoomName(dto.getRoomName());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setMaxMembers(dto.getMaxMembers());
        schedule.setBookedCount(0);
        schedule.setCancelDeadline(dto.getCancelDeadline() != null ? dto.getCancelDeadline() : 120);
        schedule.setRemark(dto.getRemark());
        schedule.setStatus(1);

        scheduleMapper.insert(schedule);
    }

    @Override
    public void update(Long id, ScheduleDTO dto) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }
        if (StringUtils.hasText(dto.getRoomName())) schedule.setRoomName(dto.getRoomName());
        if (dto.getStartTime() != null) schedule.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) schedule.setEndTime(dto.getEndTime());
        if (dto.getMaxMembers() != null) schedule.setMaxMembers(dto.getMaxMembers());
        if (dto.getCancelDeadline() != null) schedule.setCancelDeadline(dto.getCancelDeadline());
        if (StringUtils.hasText(dto.getRemark())) schedule.setRemark(dto.getRemark());
        scheduleMapper.updateById(schedule);
    }

    @Override
    public int cleanExpired() {
        return scheduleMapper.deleteExpired();
    }

    @Override
    public void cancel(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }
        schedule.setStatus(0);
        scheduleMapper.updateById(schedule);
    }

    private ScheduleVO toVO(Schedule schedule) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(schedule.getId());
        vo.setCourseId(schedule.getCourseId());
        vo.setCoachId(schedule.getCoachId());
        vo.setRoomName(schedule.getRoomName());
        vo.setStartTime(schedule.getStartTime());
        vo.setEndTime(schedule.getEndTime());
        vo.setMaxMembers(schedule.getMaxMembers());
        vo.setBookedCount(schedule.getBookedCount());
        vo.setCancelDeadline(schedule.getCancelDeadline());
        vo.setStatus(schedule.getStatus());
        vo.setRemark(schedule.getRemark());

        // 填充课程名称
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
            vo.setCourseType(course.getType());
            vo.setIntensity(course.getIntensity());
        }

        // 填充教练名称
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getName());
        }

        return vo;
    }
}
