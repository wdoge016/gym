package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.RepairOrderDTO;
import com.gym.entity.RepairOrder;
import com.gym.security.UserContext;
import com.gym.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "报修管理")
@RestController
@RequestMapping("/api/v1/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @Operation(summary = "分页查询报修单")
    @GetMapping
    public Result<PageResult<RepairOrder>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        IPage<RepairOrder> page = repairOrderService.page(current, size, status);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "获取报修详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> detail(@PathVariable Long id) {
        return Result.ok(repairOrderService.getById(id));
    }

    @Operation(summary = "会员提交报修")
    @PostMapping
    public Result<?> report(@RequestBody RepairOrderDTO dto) {
        Long memberId = UserContext.getUserId();
        repairOrderService.report(dto.getEquipmentId(), memberId, dto.getDescription());
        return Result.ok("报修提交成功");
    }

    @Operation(summary = "开始维修")
    @PutMapping("/{id}/start")
    public Result<?> startRepair(@PathVariable Long id, @RequestParam String repairPerson) {
        repairOrderService.startRepair(id, repairPerson);
        return Result.ok("维修已开始");
    }

    @Operation(summary = "完成维修")
    @PutMapping("/{id}/complete")
    public Result<?> completeRepair(@PathVariable Long id, @RequestParam(required = false) BigDecimal cost) {
        repairOrderService.completeRepair(id, cost != null ? cost : BigDecimal.ZERO);
        return Result.ok("维修完成");
    }
}
