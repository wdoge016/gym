package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.entity.Attendance;
import com.gym.exception.BusinessException;
import com.gym.mapper.AttendanceMapper;
import com.gym.service.AttendanceService;
import com.gym.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final SystemConfigService systemConfigService;

    public AttendanceServiceImpl(AttendanceMapper attendanceMapper, SystemConfigService systemConfigService) {
        this.attendanceMapper = attendanceMapper;
        this.systemConfigService = systemConfigService;
    }

    @Override
    public void checkIn(Long employeeId) {
        // 检查今天是否已打卡
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getEmployeeId, employeeId)
                .eq(Attendance::getAttendanceDate, LocalDate.now());
        if (attendanceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("今日已打卡，请勿重复签到");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now());

        // 判断是否迟到（从系统配置读取截止时间，默认09:00）
        String deadlineStr = systemConfigService.getAllAsMap().getOrDefault("check_in_deadline", "09:00");
        LocalTime deadline = LocalTime.parse(deadlineStr);
        if (LocalTime.now().isAfter(deadline)) {
            long minutes = java.time.Duration.between(deadline, LocalTime.now()).toMinutes();
            attendance.setStatus("LATE");
            attendance.setRemark("迟到" + minutes + "分钟（截止" + deadlineStr + "）");
        } else {
            attendance.setStatus("NORMAL");
        }

        attendanceMapper.insert(attendance);
    }

    @Override
    public void checkOut(Long attendanceId) {
        Attendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new BusinessException("考勤记录不存在");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new BusinessException("已签退，请勿重复操作");
        }

        attendance.setCheckOutTime(LocalTime.now());
        attendanceMapper.updateById(attendance);
    }

    @Override
    public IPage<Attendance> page(Integer current, Integer size, Long employeeId, LocalDate date) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(Attendance::getEmployeeId, employeeId);
        }
        if (date != null) {
            wrapper.eq(Attendance::getAttendanceDate, date);
        }
        wrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectPage(new Page<>(current, size), wrapper);
    }
}
