package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.entity.Attendance;
import com.gym.security.UserContext;
import com.gym.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "考勤管理")
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "签到")
    @PostMapping("/check-in")
    public Result<?> checkIn() {
        Long employeeId = UserContext.getUserId();
        attendanceService.checkIn(employeeId);
        return Result.ok("签到成功");
    }

    @Operation(summary = "签退")
    @PutMapping("/{id}/check-out")
    public Result<?> checkOut(@PathVariable Long id) {
        attendanceService.checkOut(id);
        return Result.ok("签退成功");
    }

    @Operation(summary = "考勤记录查询")
    @GetMapping
    public Result<PageResult<Attendance>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate date) {
        IPage<Attendance> page = attendanceService.page(current, size, employeeId, date);
        return Result.ok(PageResult.of(page));
    }
}
