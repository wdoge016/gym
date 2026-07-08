package com.gym.controller;

import com.gym.common.Result;
import com.gym.service.DashboardService;
import com.gym.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据统计")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取统计面板数据")
    @GetMapping("/stats")
    public Result<DashboardVO> stats() {
        return Result.ok(dashboardService.getStats());
    }
}
