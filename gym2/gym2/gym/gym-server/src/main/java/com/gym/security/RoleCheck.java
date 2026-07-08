package com.gym.security;

import com.gym.common.Constants;
import com.gym.exception.BusinessException;

import java.util.Set;

/**
 * 角色权限校验工具
 *
 * SUPER_ADMIN  — 全部
 * STORE_ADMIN  — 除员工管理外全部
 * COACH        — 仪表盘、排课、签到
 * STAFF        — 仪表盘、会员查看、签到、考勤
 */
public class RoleCheck {

    private static final Set<String> ADMIN_ONLY = Set.of(
            "employee:write", "employee:delete", "system:write"
    );

    /**
     * 需要 STORE_ADMIN 及以上的操作
     */
    public static void requireAdmin() {
        String role = UserContext.getRole();
        if (Constants.ROLE_COACH.equals(role) || Constants.ROLE_STAFF.equals(role)) {
            throw new BusinessException(403, "权限不足，需要管理员权限");
        }
    }

    /**
     * 需要 SUPER_ADMIN 的操作（员工管理）
     */
    public static void requireSuperAdmin() {
        if (!Constants.ROLE_SUPER_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(403, "权限不足，仅超级管理员可操作");
        }
    }

    /**
     * 任意登录用户可访问
     */
    public static void requireLogin() {
        if (UserContext.getUserId() == null) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
