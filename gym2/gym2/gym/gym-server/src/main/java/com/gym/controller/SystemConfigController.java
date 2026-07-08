package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.SystemConfig;
import com.gym.security.RoleCheck;
import com.gym.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统设置")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有配置")
    @GetMapping
    public Result<List<SystemConfig>> listAll() {
        return Result.ok(systemConfigService.listAll());
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{configKey}")
    public Result<?> update(@PathVariable String configKey, @RequestBody Map<String, String> body) {
        RoleCheck.requireSuperAdmin();
        systemConfigService.update(configKey, body.get("configValue"));
        return Result.ok("更新成功");
    }
}
