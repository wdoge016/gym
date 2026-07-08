package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.CoachDTO;
import com.gym.entity.Coach;
import com.gym.security.RoleCheck;
import com.gym.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教练管理")
@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @Operation(summary = "分页查询教练列表")
    @GetMapping
    public Result<PageResult<Coach>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        IPage<Coach> page = coachService.page(current, size, keyword);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "获取所有在职教练（下拉列表用）")
    @GetMapping("/all")
    public Result<List<Coach>> listAll() {
        return Result.ok(coachService.listAll());
    }

    @Operation(summary = "获取教练详情")
    @GetMapping("/{id}")
    public Result<Coach> detail(@PathVariable Long id) {
        return Result.ok(coachService.getById(id));
    }

    @Operation(summary = "新增教练")
    @PostMapping
    public Result<?> add(@RequestBody CoachDTO dto) {
        RoleCheck.requireAdmin();
        coachService.add(dto);
        return Result.ok("添加成功");
    }

    @Operation(summary = "更新教练信息")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody CoachDTO dto) {
        RoleCheck.requireAdmin();
        coachService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除教练")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleCheck.requireAdmin();
        coachService.delete(id);
        return Result.ok("删除成功");
    }
}
