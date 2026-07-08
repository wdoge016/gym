package com.gym.controller;

import com.gym.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "通用功能")
@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 校验扩展名
        String originalName = file.getOriginalFilename();
        if (originalName == null || !isAllowedExtension(originalName)) {
            return Result.fail("不支持的文件类型，仅允许 jpg/jpeg/png/gif/webp");
        }

        // 生成唯一文件名
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + ext;

        // 确保目录存在
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        // 保存
        File dest = new File(UPLOAD_DIR + newName);
        file.transferTo(dest);

        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/" + newName);
        data.put("fileName", newName);

        return Result.ok(data);
    }

    @Operation(summary = "获取验证码（简化版）")
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        // 简化实现：返回一个随机验证码文本
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        Map<String, String> data = new HashMap<>();
        data.put("captchaKey", UUID.randomUUID().toString());
        data.put("captchaCode", code);
        return Result.ok(data);
    }

    private boolean isAllowedExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }
}
