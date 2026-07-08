package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.Constants;
import com.gym.entity.Equipment;
import com.gym.entity.RepairOrder;
import com.gym.exception.BusinessException;
import com.gym.mapper.EquipmentMapper;
import com.gym.mapper.RepairOrderMapper;
import com.gym.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final EquipmentMapper equipmentMapper;

    @Override
    public IPage<RepairOrder> page(Integer current, Integer size, String status) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        return repairOrderMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public RepairOrder getById(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("报修记录不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(Long equipmentId, Long memberId, String description) {
        // 检查器材存在
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new BusinessException("器材不存在");
        }

        RepairOrder order = new RepairOrder();
        order.setEquipmentId(equipmentId);
        order.setMemberId(memberId);
        order.setDescription(description);
        order.setStatus(Constants.REPAIR_REPORTED);
        order.setReportTime(LocalDateTime.now());
        repairOrderMapper.insert(order);

        // 更新器材状态为维护中
        equipment.setStatus(Constants.EQUIP_MAINTENANCE);
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void startRepair(Long id, String repairPerson) {
        RepairOrder order = getById(id);
        if (!Constants.REPAIR_REPORTED.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可开始维修");
        }
        order.setStatus(Constants.REPAIR_IN_PROGRESS);
        order.setRepairPerson(repairPerson);
        repairOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, BigDecimal cost) {
        RepairOrder order = getById(id);
        if (!Constants.REPAIR_IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可完成维修");
        }
        order.setStatus(Constants.REPAIR_COMPLETED);
        order.setRepairTime(LocalDateTime.now());
        order.setCost(cost);
        repairOrderMapper.updateById(order);

        // 恢复器材状态
        Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus(Constants.EQUIP_NORMAL);
            equipmentMapper.updateById(equipment);
        }
    }
}
