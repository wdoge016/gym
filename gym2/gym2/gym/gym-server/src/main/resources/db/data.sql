-- =============================================
-- 智能健身房管理系统 - 初始数据
-- 先清空再插入，可重复执行
-- =============================================

-- 请在 DBeaver 左上角选择 gym_db 数据库后再执行

-- ----------------------------
-- 清空全部数据（按依赖顺序）
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reservation;
TRUNCATE TABLE wait_list;
TRUNCATE TABLE `schedule`;
TRUNCATE TABLE member_card;
TRUNCATE TABLE orders;
TRUNCATE TABLE payment;
TRUNCATE TABLE repair_order;
TRUNCATE TABLE attendance;
TRUNCATE TABLE coach;
TRUNCATE TABLE course;
TRUNCATE TABLE equipment;
TRUNCATE TABLE employee;
TRUNCATE TABLE member;
TRUNCATE TABLE coach_booking;
TRUNCATE TABLE system_config;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 员工初始数据（密码：123456）
-- ----------------------------
INSERT INTO employee (name, phone, password, role, position, department, hire_date) VALUES
('系统管理员', '13800000001', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', 'SUPER_ADMIN', '系统管理员', '管理部', '2024-01-01'),
('店长',      '13800000002', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', 'STORE_ADMIN',  '店长',     '管理部', '2024-01-01'),
('张教练',    '13800000003', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', 'COACH',       '高级教练', '教练部', '2024-03-01'),
('李教练',    '13800000004', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', 'COACH',       '中级教练', '教练部', '2024-03-15'),
('王前台',    '13800000005', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', 'STAFF',       '前台',     '运营部', '2024-02-01');

-- ----------------------------
-- 教练数据（关联员工）
-- ----------------------------
INSERT INTO coach (employee_id, name, phone, gender, speciality, description, certification, hire_date) VALUES
(3, '张教练', '13800000003', 1, '瑜伽,普拉提,冥想', '10年瑜伽教学经验，国际瑜伽联盟认证导师', '国际瑜伽联盟RYT-500', '2024-03-01'),
(4, '李教练', '13800000004', 1, '动感单车,力量训练,HIIT', '国家健身教练高级认证，擅长减脂塑形', '国家健身教练高级证书', '2024-03-15');

-- ----------------------------
-- 课程初始数据
-- ----------------------------
INSERT INTO course (name, description, type, intensity, duration, max_capacity, coach_id, price) VALUES
('流瑜伽', '通过流畅的体式串联提升身体柔韧性和力量，配合呼吸法放松身心。适合所有水平。', 'GROUP', 2, 60, 20, 1, 88.00),
('哈他瑜伽', '传统瑜伽练习，注重体式的正确对齐和呼吸控制，节奏舒缓适合初学者。', 'GROUP', 1, 60, 25, 1, 68.00),
('动感单车', '高强度有氧骑行训练，配合节奏动感的音乐燃烧脂肪，一节课消耗500-800卡路里。', 'GROUP', 4, 45, 15, 2, 78.00),
('HIIT高强度间歇', '短时间高强度爆发结合间歇恢复，最大化燃脂效率，适合有一定运动基础的会员。', 'GROUP', 5, 30, 12, 2, 98.00),
('普拉提核心训练', '专注核心肌群训练，改善体态和身体控制力，预防运动损伤。', 'GROUP', 3, 50, 18, 1, 88.00),
('私教一对一', '根据个人体质和目标定制专属训练计划，教练全程一对一指导。', 'PRIVATE', 3, 60, 1, 1, 298.00),
('减脂训练营', '为期4周的集中训练营，包含体能评估、饮食指导、集体训练和进度追踪。', 'CAMP', 4, 90, 10, 2, 1980.00);

-- ----------------------------
-- 会员初始数据（密码：123456）
-- ----------------------------
INSERT INTO member (username, password, real_name, phone, gender, credit_score) VALUES
('member01', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', '张三', '13900000001', 1, 100),
('member02', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', '李四', '13900000002', 2, 95),
('member03', '$2b$12$FE5Fvgp4M3CdqzsCGnu4a.9e4/A7vrUJWt0MgbLuthMo32VIpVMR2', '王五', '13900000003', 1, 80);

-- ----------------------------
-- 排课初始数据
-- ----------------------------
INSERT INTO `schedule` (course_id, coach_id, room_name, start_time, end_time, max_members, booked_count, cancel_deadline) VALUES
(1, 1, '瑜伽教室A', '2026-06-26 09:00:00', '2026-06-26 10:00:00', 20, 0, 120),
(3, 2, '单车教室',  '2026-06-26 10:30:00', '2026-06-26 11:15:00', 15, 0, 120),
(2, 1, '瑜伽教室B', '2026-06-26 14:00:00', '2026-06-26 15:00:00', 25, 0, 120),
(4, 2, '多功能厅',  '2026-06-27 08:00:00', '2026-06-27 08:30:00', 12, 0, 120),
(5, 1, '瑜伽教室A', '2026-06-27 10:00:00', '2026-06-27 10:50:00', 18, 0, 120),
(1, 1, '瑜伽教室A', '2026-06-28 09:00:00', '2026-06-28 10:00:00', 20, 0, 120),
(3, 2, '单车教室',  '2026-06-28 18:00:00', '2026-06-28 18:45:00', 15, 0, 120);

-- ----------------------------
-- 器材初始数据
-- ----------------------------
INSERT INTO equipment (name, equipment_type, target_muscle, video_url, warnings, location, purchase_date) VALUES
('跑步机 Pro-Runner X1', 'CARDIO', '全身,腿部,心肺', 'https://example.com/videos/treadmill.mp4', '使用前请确认紧急停止装置正常；心率异常请立即停止；建议从低速开始逐步加速', '有氧区', '2024-01-15'),
('史密斯机 SM-300', 'STRENGTH', '胸肌,肩部,肱三头肌', 'https://example.com/videos/smith.mp4', '深蹲时请使用保护杠；卧推请勿锁死肘关节；初学者需在教练指导下使用', '力量区', '2024-02-20'),
('可调节哑铃套装', 'FREE_WEIGHT', '全身', 'https://example.com/videos/dumbbell.mp4', '训练前检查铃片是否锁紧；请勿摔砸哑铃；使用后放回原处', '自由力量区', '2024-03-10'),
('龙门架 Cable Pro', 'FUNCTIONAL', '全身,核心', 'https://example.com/videos/cable.mp4', '调整滑轮高度时注意手指安全；拉力绳磨损严重请报修更换', '功能训练区', '2024-01-20'),
('瑜伽垫套装', 'ACCESSORY', '全身,柔韧', NULL, '使用后请用消毒湿巾擦拭；破损请及时报修', '瑜伽教室', '2024-04-01');

-- ----------------------------
-- 会员卡初始数据
-- ----------------------------
INSERT INTO member_card (member_id, card_type, total_times, remaining_times, start_date, end_date, amount, status) VALUES
(1, 'YEAR', 0, -1, '2026-01-01', '2026-12-31', 2999.00, 1),
(2, 'QUARTER', 0, -1, '2026-04-01', '2026-06-30', 999.00, 1),
(3, 'MONTH', 0, -1, '2026-05-01', '2026-12-31', 299.00, 1);

-- ----------------------------
-- 系统配置初始数据
-- ----------------------------
INSERT INTO system_config (config_key, config_value, description) VALUES
('credit_score_min', '60', '最低预约信用分'),
('credit_score_penalty', '10', '爽约扣分值'),
('cancel_deadline', '120', '退课截止时间(分钟)'),
('card_month_price', '299', '月卡价格'),
('card_quarter_price', '799', '季卡价格'),
('card_year_price', '2599', '年卡价格'),
('check_in_deadline', '09:00', '签到截止时间');
