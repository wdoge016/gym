package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.entity.Attendance;

import java.time.LocalDate;

public interface AttendanceService {

    void checkIn(Long employeeId);

    void checkOut(Long attendanceId);

    IPage<Attendance> page(Integer current, Integer size, Long employeeId, LocalDate date);
}
