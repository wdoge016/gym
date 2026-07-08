package com.gym.common;

/**
 * 系统常量
 */
public class Constants {

    // ========== 角色 ==========
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_STORE_ADMIN = "STORE_ADMIN";
    public static final String ROLE_COACH = "COACH";
    public static final String ROLE_STAFF = "STAFF";

    // ========== 会员卡类型 ==========
    public static final String CARD_TYPE_MONTH = "MONTH";
    public static final String CARD_TYPE_QUARTER = "QUARTER";
    public static final String CARD_TYPE_YEAR = "YEAR";

    // ========== 课程类型 ==========
    public static final String COURSE_TYPE_GROUP = "GROUP";
    public static final String COURSE_TYPE_PRIVATE = "PRIVATE";
    public static final String COURSE_TYPE_CAMP = "CAMP";

    // ========== 预约来源 ==========
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_AI_RECOMMEND = "AI_RECOMMEND";
    public static final String SOURCE_WAITLIST = "WAITLIST";

    // ========== 预约状态 ==========
    public static final String RES_STATUS_BOOKED = "BOOKED";
    public static final String RES_STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String RES_STATUS_CANCELLED = "CANCELLED";
    public static final String RES_STATUS_ABSENT = "ABSENT";

    // ========== 订单类型 ==========
    public static final String ORDER_TYPE_CARD_PURCHASE = "CARD_PURCHASE";
    public static final String ORDER_TYPE_COURSE_BOOKING = "COURSE_BOOKING";
    public static final String ORDER_TYPE_RENEWAL = "RENEWAL";

    // ========== 订单/支付状态 ==========
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    // ========== 器材状态 ==========
    public static final String EQUIP_NORMAL = "NORMAL";
    public static final String EQUIP_MAINTENANCE = "MAINTENANCE";
    public static final String EQUIP_SCRAPPED = "SCRAPPED";

    // ========== 报修状态 ==========
    public static final String REPAIR_REPORTED = "REPORTED";
    public static final String REPAIR_IN_PROGRESS = "IN_PROGRESS";
    public static final String REPAIR_COMPLETED = "COMPLETED";

    // ========== 信用分 ==========
    public static final int CREDIT_SCORE_DEFAULT = 100;
    public static final int CREDIT_SCORE_MIN_BOOK = 60;
    public static final int CREDIT_SCORE_ABSENT_PENALTY = 10;

    // ========== JWT ==========
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_TYPE = "userType";
    public static final String CLAIM_ROLE = "role";
    public static final String USER_TYPE_MEMBER = "MEMBER";
    public static final String USER_TYPE_EMPLOYEE = "EMPLOYEE";

    // ========== Redis Key ==========
    public static final String REDIS_TOKEN_BLACKLIST = "blacklist:token:";
}
