package com.gym.controller;

import com.gym.common.Result;
import com.gym.dto.LoginDTO;
import com.gym.service.EmployeeAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "员工认证")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class EmployeeAuthController {

    private final EmployeeAuthService employeeAuthService;

    @Operation(summary = "员工后台登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(employeeAuthService.login(dto));
    }
}
