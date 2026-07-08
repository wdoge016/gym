package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.EquipmentDTO;
import com.gym.entity.Equipment;
import com.gym.security.RoleCheck;
import com.gym.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "器材管理")
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "分页查询器材")
    @GetMapping
    public Result<PageResult<Equipment>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        IPage<Equipment> page = equipmentService.page(current, size, keyword, type);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "获取器材详情")
    @GetMapping("/{id}")
    public Result<Equipment> detail(@PathVariable Long id) {
        return Result.ok(equipmentService.getById(id));
    }

    @Operation(summary = "新增器材")
    @PostMapping
    public Result<?> add(@RequestBody EquipmentDTO dto) {
        RoleCheck.requireAdmin();
        equipmentService.add(dto);
        return Result.ok("添加成功");
    }

    @Operation(summary = "更新器材")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody EquipmentDTO dto) {
        RoleCheck.requireAdmin();
        equipmentService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "更新器材状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        RoleCheck.requireAdmin();
        equipmentService.updateStatus(id, status);
        return Result.ok("状态更新成功");
    }

    @Operation(summary = "删除器材")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleCheck.requireAdmin();
        equipmentService.delete(id);
        return Result.ok("删除成功");
    }
}
