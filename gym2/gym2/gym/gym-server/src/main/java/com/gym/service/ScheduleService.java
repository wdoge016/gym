package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.ScheduleDTO;
import com.gym.entity.Schedule;
import com.gym.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    IPage<ScheduleVO> page(Integer current, Integer size, Long courseId, LocalDate date);

    List<ScheduleVO> listAvailable();

    ScheduleVO getById(Long id);

    void add(ScheduleDTO dto);

    void update(Long id, ScheduleDTO dto);

    void cancel(Long id);

    /**
     * 清理所有已过期的排课（endTime < now）
     * @return 清理数量
     */
    int cleanExpired();
}
