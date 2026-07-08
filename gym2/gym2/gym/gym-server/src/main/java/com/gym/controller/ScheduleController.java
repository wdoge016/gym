package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.ScheduleDTO;
import com.gym.security.RoleCheck;
import com.gym.service.ScheduleService;
import com.gym.vo.ScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "排课管理")
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "分页查询排课")
    @GetMapping
    public Result<PageResult<ScheduleVO>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) LocalDate date) {
        IPage<ScheduleVO> page = scheduleService.page(current, size, courseId, date);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "查询可预约排课")
    @GetMapping("/available")
    public Result<List<ScheduleVO>> available() {
        return Result.ok(scheduleService.listAvailable());
    }

    @Operation(summary = "获取排课详情")
    @GetMapping("/{id}")
    public Result<ScheduleVO> detail(@PathVariable Long id) {
        return Result.ok(scheduleService.getById(id));
    }

    @Operation(summary = "新增排课")
    @PostMapping
    public Result<?> add(@RequestBody ScheduleDTO dto) {
        RoleCheck.requireAdmin();
        scheduleService.add(dto);
        return Result.ok("排课成功");
    }

    @Operation(summary = "更新排课")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ScheduleDTO dto) {
        RoleCheck.requireAdmin();
        scheduleService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "取消排课")
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        RoleCheck.requireAdmin();
        scheduleService.cancel(id);
        return Result.ok("取消成功");
    }

    @Operation(summary = "一键清理过期排课")
    @DeleteMapping("/clean-expired")
    public Result<?> cleanExpired() {
        RoleCheck.requireAdmin();
        int count = scheduleService.cleanExpired();
        return Result.ok("清理完成，共删除 " + count + " 条过期排课");
    }
}
