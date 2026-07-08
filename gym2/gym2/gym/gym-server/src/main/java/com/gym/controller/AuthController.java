package com.gym.controller;

import com.gym.common.Constants;
import com.gym.common.Result;
import com.gym.dto.ChangePasswordDTO;
import com.gym.dto.LoginDTO;
import com.gym.dto.RegisterDTO;
import com.gym.security.UserContext;
import com.gym.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "会员认证")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok("注册成功");
    }

    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(Constants.TOKEN_HEADER) String authHeader) {
        String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
        authService.logout(token);
        return Result.ok("退出成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<?> userinfo() {
        Long userId = UserContext.getUserId();
        return Result.ok(authService.getUserInfo(userId));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.ok("密码修改成功");
    }
}
