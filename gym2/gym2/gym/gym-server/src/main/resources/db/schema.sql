-- =============================================
-- 智能健身房管理系统 - 数据库建表脚本（14张表）
-- =============================================

CREATE DATABASE IF NOT EXISTS gym_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 请在 DBeaver 左上角数据库选择器中选择 gym_db 后再执行以下内容

-- ----------------------------
-- 1. member 会员表
-- ----------------------------
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL COMMENT 'BCrypt加密',
    real_name       VARCHAR(50),
    phone           VARCHAR(20)     UNIQUE,
    gender          TINYINT         DEFAULT 0 COMMENT '0未知 1男 2女',
    credit_score    INT             DEFAULT 100 COMMENT '信用分，默认100',
    avatar_url      VARCHAR(255),
    status          TINYINT         DEFAULT 1 COMMENT '1正常 0禁用',
    last_login_time DATETIME,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- ----------------------------
-- 2. member_card 会员卡表
-- ----------------------------
DROP TABLE IF EXISTS member_card;
CREATE TABLE member_card (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT          NOT NULL COMMENT '会员ID',
    card_type       VARCHAR(20)     NOT NULL COMMENT 'MONTH/QUARTER/YEAR',
    total_times     INT             DEFAULT 0,
    remaining_times INT             DEFAULT -1 COMMENT '-1=无限次',
    start_date      DATE            NOT NULL,
    end_date        DATE,
    amount          DECIMAL(10,2)   NOT NULL COMMENT '购买金额',
    status          TINYINT         DEFAULT 1 COMMENT '1生效 0过期',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员卡表';

-- ----------------------------
-- 3. course 课程表
-- ----------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    type            VARCHAR(20)     NOT NULL COMMENT 'GROUP/PRIVATE/CAMP',
    intensity       INT             DEFAULT 1 COMMENT '强度 1~5',
    duration        INT             NOT NULL COMMENT '时长(分钟)',
    max_capacity    INT             NOT NULL COMMENT '最大容量',
    coach_id        BIGINT,
    price           DECIMAL(10,2)   NOT NULL COMMENT '价格',
    cover_image     VARCHAR(255),
    status          TINYINT         DEFAULT 1 COMMENT '1上架 0下架',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_coach_id (coach_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ----------------------------
-- 4. coach 教练表
-- ----------------------------
DROP TABLE IF EXISTS coach;
CREATE TABLE coach (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT          COMMENT '关联员工ID',
    name            VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20),
    gender          TINYINT         DEFAULT 0 COMMENT '0未知 1男 2女',
    speciality      VARCHAR(200)    COMMENT '专长',
    description     TEXT            COMMENT '简介',
    avatar_url      VARCHAR(255),
    certification   VARCHAR(255)    COMMENT '资质证书',
    status          TINYINT         DEFAULT 1 COMMENT '1在职 0离职',
    hire_date       DATE,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';

-- ----------------------------
-- 5. schedule 排课表
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    coach_id        BIGINT          NOT NULL,
    room_name       VARCHAR(100)    NOT NULL COMMENT '教室名称',
    start_time      DATETIME        NOT NULL,
    end_time        DATETIME        NOT NULL,
    max_members     INT             NOT NULL COMMENT '最大人数',
    booked_count    INT             DEFAULT 0 COMMENT '已预约人数',
    cancel_deadline INT             DEFAULT 120 COMMENT '退课截止(分钟)，课前N分钟不可退',
    status          TINYINT         DEFAULT 1 COMMENT '1正常 0取消',
    remark          VARCHAR(500),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_course_id (course_id),
    INDEX idx_coach_id (coach_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';

-- ----------------------------
-- 6. reservation 预约表
-- ----------------------------
DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT          NOT NULL,
    schedule_id     BIGINT          NOT NULL,
    source          VARCHAR(20)     DEFAULT 'MANUAL' COMMENT 'MANUAL/AI_RECOMMEND/WAITLIST',
    status          VARCHAR(20)     DEFAULT 'BOOKED' COMMENT 'BOOKED/CHECKED_IN/CANCELLED/ABSENT',
    book_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancel_time     DATETIME,
    check_in_time   DATETIME,
    cancel_reason   VARCHAR(255),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_member_id (member_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ----------------------------
-- 7. wait_list 候补表
-- ----------------------------
DROP TABLE IF EXISTS wait_list;
CREATE TABLE wait_list (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT          NOT NULL,
    schedule_id     BIGINT          NOT NULL,
    status          VARCHAR(20)     DEFAULT 'WAITING' COMMENT 'WAITING/PROMOTED/CANCELLED',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_schedule_id (schedule_id),
    UNIQUE KEY uk_member_schedule (member_id, schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候补表';

-- ----------------------------
-- 8. orders 订单表（实体名 Order）
-- ----------------------------
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(32)     NOT NULL UNIQUE COMMENT '订单号(Snowflake)',
    member_id       BIGINT          NOT NULL,
    order_type      VARCHAR(20)     NOT NULL COMMENT 'CARD_PURCHASE/COURSE_BOOKING/RENEWAL',
    amount          DECIMAL(10,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CANCELLED/REFUNDED',
    pay_time        DATETIME,
    remark          VARCHAR(500),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_member_id (member_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 9. payment 支付表
-- ----------------------------
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    pay_method      VARCHAR(20)     NOT NULL COMMENT 'WECHAT/ALIPAY/CARD/CASH',
    amount          DECIMAL(10,2)   NOT NULL,
    transaction_no  VARCHAR(64)     COMMENT '幂等键',
    status          VARCHAR(20)     DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    pay_time        DATETIME,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    UNIQUE KEY uk_transaction_no (transaction_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付表';

-- ----------------------------
-- 10. employee 员工表（兼后台用户）
-- ----------------------------
DROP TABLE IF EXISTS employee;
CREATE TABLE employee (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20)     UNIQUE,
    password        VARCHAR(255)    NOT NULL COMMENT 'BCrypt加密',
    role            VARCHAR(50)     NOT NULL DEFAULT 'STAFF' COMMENT 'SUPER_ADMIN/STORE_ADMIN/COACH/STAFF',
    position        VARCHAR(50)     COMMENT '职位',
    department      VARCHAR(50)     COMMENT '部门',
    salary          DECIMAL(10,2),
    hire_date       DATE,
    status          TINYINT         DEFAULT 1 COMMENT '1在职 0离职',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表（后台用户）';

-- ----------------------------
-- 11. attendance 考勤表
-- ----------------------------
DROP TABLE IF EXISTS attendance;
CREATE TABLE attendance (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT          NOT NULL,
    attendance_date DATE            NOT NULL,
    check_in_time   TIME,
    check_out_time  TIME,
    status          VARCHAR(20)     DEFAULT 'NORMAL' COMMENT 'NORMAL/LATE/EARLY/ABSENT',
    remark          VARCHAR(255),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_employee_date (employee_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- ----------------------------
-- 12. equipment 器材表
-- ----------------------------
DROP TABLE IF EXISTS equipment;
CREATE TABLE equipment (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    equipment_type  VARCHAR(30)     NOT NULL COMMENT 'CARDIO/STRENGTH/FREE_WEIGHT/FUNCTIONAL/ACCESSORY',
    target_muscle   VARCHAR(100)    COMMENT '目标肌群',
    video_url       VARCHAR(255)    COMMENT '动作示范视频',
    warnings        TEXT            COMMENT '注意事项',
    purchase_date   DATE,
    status          VARCHAR(20)     DEFAULT 'NORMAL' COMMENT 'NORMAL/MAINTENANCE/SCRAPPED',
    location        VARCHAR(100)    COMMENT '位置',
    description     TEXT,
    image_url       VARCHAR(255),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材表';

-- ----------------------------
-- 13. repair_order 报修表
-- ----------------------------
DROP TABLE IF EXISTS repair_order;
CREATE TABLE repair_order (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    equipment_id    BIGINT          NOT NULL,
    member_id       BIGINT          NOT NULL COMMENT '报修会员',
    description     TEXT            NOT NULL,
    status          VARCHAR(20)     DEFAULT 'REPORTED' COMMENT 'REPORTED/IN_PROGRESS/COMPLETED',
    report_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    repair_time     DATETIME,
    cost            DECIMAL(10,2),
    repair_person   VARCHAR(50),
    remark          VARCHAR(500),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0,
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修表';

-- ----------------------------
-- 14. system_config 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    config_key      VARCHAR(50)     NOT NULL UNIQUE COMMENT '配置键',
    config_value    VARCHAR(200)    NOT NULL COMMENT '配置值',
    description     VARCHAR(200)    COMMENT '说明',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ----------------------------
-- 15. coach_booking 教练预约表
-- ----------------------------
DROP TABLE IF EXISTS coach_booking;
CREATE TABLE coach_booking (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT          NOT NULL,
    coach_id         BIGINT          NOT NULL,
    appointment_date DATE            NOT NULL,
    start_time       TIME            NOT NULL,
    end_time         TIME            NOT NULL,
    status           VARCHAR(20)     DEFAULT 'BOOKED' COMMENT 'BOOKED/CANCELLED/COMPLETED',
    remark           VARCHAR(500)    COMMENT '会员需求备注',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT         DEFAULT 0,
    INDEX idx_member_id (member_id),
    INDEX idx_coach_id (coach_id),
    INDEX idx_date (appointment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练预约表';
