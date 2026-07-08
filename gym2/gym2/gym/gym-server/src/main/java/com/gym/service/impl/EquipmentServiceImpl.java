package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.EquipmentDTO;
import com.gym.entity.Equipment;
import com.gym.exception.BusinessException;
import com.gym.mapper.EquipmentMapper;
import com.gym.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;

    @Override
    public IPage<Equipment> page(Integer current, Integer size, String keyword, String type) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Equipment::getName, keyword);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Equipment::getEquipmentType, type);
        }
        wrapper.orderByDesc(Equipment::getCreateTime);
        return equipmentMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Equipment getById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("器材不存在");
        }
        return equipment;
    }

    @Override
    public void add(EquipmentDTO dto) {
        Equipment equipment = new Equipment();
        equipment.setName(dto.getName());
        equipment.setEquipmentType(dto.getEquipmentType());
        equipment.setTargetMuscle(dto.getTargetMuscle());
        equipment.setWarnings(dto.getWarnings());
        equipment.setPurchaseDate(dto.getPurchaseDate());
        equipment.setLocation(dto.getLocation());
        equipment.setDescription(dto.getDescription());
        equipment.setStatus("NORMAL");
        equipmentMapper.insert(equipment);
    }

    @Override
    public void update(Long id, EquipmentDTO dto) {
        Equipment equipment = getById(id);
        if (StringUtils.hasText(dto.getName())) equipment.setName(dto.getName());
        if (StringUtils.hasText(dto.getEquipmentType())) equipment.setEquipmentType(dto.getEquipmentType());
        if (StringUtils.hasText(dto.getTargetMuscle())) equipment.setTargetMuscle(dto.getTargetMuscle());
        if (StringUtils.hasText(dto.getWarnings())) equipment.setWarnings(dto.getWarnings());
        if (dto.getPurchaseDate() != null) equipment.setPurchaseDate(dto.getPurchaseDate());
        if (StringUtils.hasText(dto.getLocation())) equipment.setLocation(dto.getLocation());
        if (StringUtils.hasText(dto.getDescription())) equipment.setDescription(dto.getDescription());
        if (StringUtils.hasText(dto.getImageUrl())) equipment.setImageUrl(dto.getImageUrl());
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Equipment equipment = getById(id);
        equipment.setStatus(status);
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void delete(Long id) {
        equipmentMapper.deleteById(id);
    }
}
