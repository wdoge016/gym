package com.gym.controller;

import com.gym.common.Result;
import com.gym.dto.ChatDTO;
import com.gym.security.UserContext;
import com.gym.service.AiService;
import com.gym.vo.ChatVO;
import com.gym.vo.RecommendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI智能推荐")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI课程顾问对话")
    @PostMapping("/chat")
    public Result<ChatVO> chat(@RequestBody ChatDTO dto) {
        Long memberId = UserContext.getUserId();
        return Result.ok(aiService.chat(memberId, dto.getMessage()));
    }

    @Operation(summary = "AI推荐课程")
    @PostMapping("/recommend")
    public Result<RecommendVO> recommend() {
        Long memberId = UserContext.getUserId();
        return Result.ok(aiService.recommend(memberId));
    }
}
