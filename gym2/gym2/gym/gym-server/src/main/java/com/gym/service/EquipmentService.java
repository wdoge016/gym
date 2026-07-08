package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.EquipmentDTO;
import com.gym.entity.Equipment;

public interface EquipmentService {

    IPage<Equipment> page(Integer current, Integer size, String keyword, String type);

    Equipment getById(Long id);

    void add(EquipmentDTO dto);

    void update(Long id, EquipmentDTO dto);

    void updateStatus(Long id, String status);

    void delete(Long id);
}
