package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.Coach;
import com.gym.entity.Course;
import com.gym.entity.Member;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.CourseMapper;
import com.gym.mapper.MemberMapper;
import com.gym.service.AiService;
import com.gym.vo.ChatVO;
import com.gym.vo.RecommendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 服务实现：直接调用 DeepSeek API（OpenAI 兼容接口）
 * 失败时自动回退到规则推荐
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;

    @Value("${deepseek.api-key:sk-your-api-key}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    public AiServiceImpl(RestTemplate restTemplate, CourseMapper courseMapper,
                         CoachMapper coachMapper, MemberMapper memberMapper) {
        this.restTemplate = restTemplate;
        this.courseMapper = courseMapper;
        this.coachMapper = coachMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    public ChatVO chat(Long memberId, String message) {
        Member member = memberMapper.selectById(memberId);
        String userName = member != null ? member.getRealName() : "会员";

        try {
            String response = callDeepSeek(userName, message);
            return ChatVO.builder()
                    .role("assistant")
                    .content(response)
                    .build();
        } catch (Exception e) {
            log.warn("DeepSeek调用失败，使用规则回退: {}", e.getMessage());
            return ChatVO.builder()
                    .role("assistant")
                    .content(fallbackChat(userName, message))
                    .build();
        }
    }

    @Override
    public RecommendVO recommend(Long memberId) {
        Member member = memberMapper.selectById(memberId);

        try {
            String prompt = buildRecommendPrompt(member);
            String response = callDeepSeek("会员", prompt);
            return parseAndMatchRecommend(response);
        } catch (Exception e) {
            log.warn("AI推荐失败，使用规则回退: {}", e.getMessage());
            return fallbackRecommend();
        }
    }

    /**
     * 调用 DeepSeek API（OpenAI 兼容格式）
     */
    private String callDeepSeek(String userName, String userMessage) {
        String url = baseUrl + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = String.format(
            "你是智能健身房AI顾问。会员叫%s，仅首次打招呼时说一次'XX先生/女士好'，之后对话绝对不要重复名字。用中文简短回答（80字内），直接给答案不要寒暄。", userName);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        throw new RuntimeException("DeepSeek返回格式异常");
    }

    // ========== Prompt 构建 ==========

    private String buildChatPrompt(String userName, String message) {
        return String.format("当前会员：%s\n会员问题：%s\n请用中文简短回答（100字以内）。", userName, message);
    }

    private String buildRecommendPrompt(Member member) {
        String gender = member != null && member.getGender() == 1 ? "男" : "女";
        int score = member != null ? member.getCreditScore() : 100;
        return String.format(
            "根据以下会员信息推荐3门课程：性别-%s，信用分-%d。" +
            "可选类型：GROUP(团课)/PRIVATE(私教)/CAMP(训练营)，强度1-5。" +
            "请以JSON格式返回：{\"courses\":[{\"type\":\"GROUP\",\"intensity\":2,\"reason\":\"理由\"}]}",
            gender, score);
    }

    // ========== 回退逻辑 ==========

    private String fallbackChat(String userName, String message) {
        if (message.contains("推荐") || message.contains("课程")) {
            return "您好 " + userName + "！为您推荐以下课程：流瑜伽（适合放松减压）、动感单车（高效燃脂）、HIIT（快速塑形）。您想了解哪个课程呢？";
        }
        if (message.contains("减肥") || message.contains("减脂")) {
            return "建议您尝试动感单车和HIIT高强度间歇训练，每节课可消耗500-800卡路里，配合饮食效果更佳！";
        }
        if (message.contains("瑜伽") || message.contains("放松")) {
            return "我们有流瑜伽、哈他瑜伽和普拉提课程，适合不同水平。初学者建议从哈他瑜伽开始，舒缓节奏易上手。";
        }
        return "您好！我是您的专属健身顾问，可以帮您推荐课程、解答健身问题。请问有什么可以帮您的？";
    }

    private RecommendVO fallbackRecommend() {
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1)
                        .orderByAsc(Course::getIntensity)
                        .last("LIMIT 3"));

        RecommendVO vo = new RecommendVO();
        vo.setReason("根据课程热度为您推荐以下课程：");

        List<RecommendVO.CourseItem> items = courses.stream().map(c -> {
            RecommendVO.CourseItem item = new RecommendVO.CourseItem();
            item.setCourseId(c.getId());
            item.setCourseName(c.getName());
            item.setType(c.getType());
            item.setIntensity(c.getIntensity());
            item.setPrice(c.getPrice());

            Coach coach = coachMapper.selectById(c.getCoachId());
            if (coach != null) item.setCoachName(coach.getName());

            return item;
        }).collect(Collectors.toList());

        vo.setCourses(items);
        return vo;
    }

    private RecommendVO parseAndMatchRecommend(String aiResponse) {
        List<Course> allCourses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1));

        List<Course> matched = allCourses.stream()
                .filter(c -> aiResponse.contains(c.getType()) || aiResponse.contains(c.getName()))
                .limit(3)
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            matched = allCourses.stream().limit(3).collect(Collectors.toList());
        }

        RecommendVO vo = new RecommendVO();
        vo.setReason("AI智能推荐（基于您的偏好分析）：");

        List<RecommendVO.CourseItem> items = matched.stream().map(c -> {
            RecommendVO.CourseItem item = new RecommendVO.CourseItem();
            item.setCourseId(c.getId());
            item.setCourseName(c.getName());
            item.setType(c.getType());
            item.setIntensity(c.getIntensity());
            item.setPrice(c.getPrice());

            Coach coach = coachMapper.selectById(c.getCoachId());
            if (coach != null) item.setCoachName(coach.getName());

            return item;
        }).collect(Collectors.toList());

        vo.setCourses(items);
        return vo;
    }
}
