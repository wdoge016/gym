package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.CoachDTO;
import com.gym.entity.Coach;

import java.util.List;

public interface CoachService {

    IPage<Coach> page(Integer current, Integer size, String keyword);

    List<Coach> listAll();

    Coach getById(Long id);

    void add(CoachDTO dto);

    void update(Long id, CoachDTO dto);

    void delete(Long id);
}
