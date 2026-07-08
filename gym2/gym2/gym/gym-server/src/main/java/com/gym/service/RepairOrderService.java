package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.entity.RepairOrder;

public interface RepairOrderService {

    IPage<RepairOrder> page(Integer current, Integer size, String status);

    RepairOrder getById(Long id);

    void report(Long equipmentId, Long memberId, String description);

    void startRepair(Long id, String repairPerson);

    void completeRepair(Long id, java.math.BigDecimal cost);
}
