package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.EmployeeDTO;
import com.gym.entity.Employee;
import com.gym.security.RoleCheck;
import com.gym.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "员工管理")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping
    public Result<PageResult<Employee>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        IPage<Employee> page = employeeService.page(current, size, keyword);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/{id}")
    public Result<Employee> detail(@PathVariable Long id) {
        return Result.ok(employeeService.getById(id));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    public Result<?> add(@RequestBody EmployeeDTO dto) {
        RoleCheck.requireSuperAdmin();
        employeeService.add(dto);
        return Result.ok("添加成功");
    }

    @Operation(summary = "更新员工")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        RoleCheck.requireSuperAdmin();
        employeeService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除员工（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.ok("删除成功");
    }

    @Operation(summary = "更新员工状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        employeeService.updateStatus(id, status);
        return Result.ok("状态更新成功");
    }
}
