package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.CourseDTO;
import com.gym.entity.Course;
import com.gym.security.RoleCheck;
import com.gym.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "分页查询课程列表")
    @GetMapping
    public Result<PageResult<Course>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        IPage<Course> page = courseService.page(current, size, keyword, type);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "按类型查询课程")
    @GetMapping("/type/{type}")
    public Result<List<Course>> listByType(@PathVariable String type) {
        return Result.ok(courseService.listByType(type));
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.ok(courseService.getById(id));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public Result<?> add(@RequestBody CourseDTO dto) {
        RoleCheck.requireAdmin();
        courseService.add(dto);
        return Result.ok("添加成功");
    }

    @Operation(summary = "更新课程信息")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        RoleCheck.requireAdmin();
        courseService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleCheck.requireAdmin();
        courseService.delete(id);
        return Result.ok("删除成功");
    }
}
