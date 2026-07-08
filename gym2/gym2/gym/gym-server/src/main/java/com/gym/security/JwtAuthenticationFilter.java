package com.gym.security;

import com.gym.common.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * - 解析 Token 并存入 UserContext
 * - 检查 Redis Token 黑名单（Redis 不可用时自动跳过）
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;
        }

        // 检查 Token 黑名单（Redis不可用时跳过）
        try {
            String blacklistKey = Constants.REDIS_TOKEN_BLACKLIST + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                return false;
            }
        } catch (Exception e) {
            // Redis 不可用，跳过黑名单检查
        }

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        // 存入 ThreadLocal
        UserContext.setUserId(jwtUtil.getUserId(token));
        UserContext.setUserType(jwtUtil.getUserType(token));
        UserContext.setRole(jwtUtil.getRole(token));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(Constants.TOKEN_PREFIX)) {
            return header.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
