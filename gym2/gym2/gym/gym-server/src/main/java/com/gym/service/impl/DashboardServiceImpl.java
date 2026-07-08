package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.entity.*;
import com.gym.mapper.*;
import com.gym.service.DashboardService;
import com.gym.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final ReservationMapper reservationMapper;
    private final OrderMapper orderMapper;
    private final ScheduleMapper scheduleMapper;
    private final CoachMapper coachMapper;
    private final EquipmentMapper equipmentMapper;
    private final CourseMapper courseMapper;

    @Override
    public DashboardVO getStats() {
        DashboardVO vo = new DashboardVO();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

        // ---- 核心指标 ----

        // 1. 本月收入
        YearMonth currentMonth = YearMonth.now();
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getStatus, Constants.STATUS_PAID)
                .between(Order::getCreateTime,
                        LocalDateTime.of(currentMonth.atDay(1), LocalTime.MIN),
                        LocalDateTime.of(currentMonth.atEndOfMonth(), LocalTime.MAX));
        List<Order> paidOrders = orderMapper.selectList(orderWrapper);
        BigDecimal monthIncome = paidOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setMonthIncome(monthIncome);

        // 2. 今日新增会员
        LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.between(Member::getCreateTime, todayStart, todayEnd);
        vo.setTodayNewMembers(memberMapper.selectCount(memberWrapper));

        // 3. 今日新增预约
        LambdaQueryWrapper<Reservation> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.between(Reservation::getCreateTime, todayStart, todayEnd);
        vo.setTodayBookings(reservationMapper.selectCount(bookingWrapper));

        // 4. 今日排课数
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getStatus, 1)
                .between(Schedule::getStartTime, todayStart, todayEnd);
        vo.setTodaySchedules(scheduleMapper.selectCount(scheduleWrapper));

        // ---- 辅助指标 ----

        // 5. 会员总数
        vo.setTotalMembers(memberMapper.selectCount(null));

        // 6. 在职教练数
        LambdaQueryWrapper<Coach> coachWrapper = new LambdaQueryWrapper<>();
        coachWrapper.eq(Coach::getStatus, 1);
        vo.setTotalCoaches(coachMapper.selectCount(coachWrapper));

        // 7. 正常器材数
        LambdaQueryWrapper<Equipment> equipWrapper = new LambdaQueryWrapper<>();
        equipWrapper.eq(Equipment::getStatus, "NORMAL");
        vo.setTotalEquipment(equipmentMapper.selectCount(equipWrapper));

        // 8. 留存率
        vo.setRetentionRate(calculateRetentionRate());

        // 9. 满座率
        vo.setOccupancyRate(calculateOccupancyRate());

        // ---- 热门课程 TOP8（含已预约和已签到） ----
        LambdaQueryWrapper<Reservation> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.in(Reservation::getStatus, Constants.RES_STATUS_BOOKED, Constants.RES_STATUS_CHECKED_IN);
        List<Reservation> allRes = reservationMapper.selectList(topWrapper);

        Map<Long, Long> scheduleCountMap = allRes.stream()
                .collect(Collectors.groupingBy(Reservation::getScheduleId, Collectors.counting()));

        List<DashboardVO.HotCourse> hotCourses = scheduleCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> {
                    DashboardVO.HotCourse hc = new DashboardVO.HotCourse();
                    Schedule schedule = scheduleMapper.selectById(entry.getKey());
                    if (schedule != null) {
                        Course course = courseMapper.selectById(schedule.getCourseId());
                        hc.setCourseName(course != null ? course.getName() : "未知课程");
                    } else {
                        hc.setCourseName("未知课程");
                    }
                    hc.setBookingCount(entry.getValue().intValue());
                    return hc;
                })
                .collect(Collectors.toList());
        vo.setHotCourses(hotCourses);

        return vo;
    }

    private BigDecimal calculateRetentionRate() {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStatus, Constants.RES_STATUS_CHECKED_IN);
        List<Reservation> all = reservationMapper.selectList(wrapper);
        if (all.isEmpty()) return BigDecimal.ZERO;

        Map<Long, Long> memberCheckins = all.stream()
                .collect(Collectors.groupingBy(Reservation::getMemberId, Collectors.counting()));
        long retained = memberCheckins.values().stream().filter(c -> c > 1).count();
        long total = memberCheckins.size();
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(retained)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateOccupancyRate() {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getStatus, 1)
                .gt(Schedule::getStartTime, LocalDateTime.now().minusDays(30));
        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        if (schedules.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalRate = BigDecimal.ZERO;
        int count = 0;
        for (Schedule s : schedules) {
            if (s.getMaxMembers() != null && s.getMaxMembers() > 0) {
                totalRate = totalRate.add(
                        BigDecimal.valueOf(s.getBookedCount())
                                .divide(BigDecimal.valueOf(s.getMaxMembers()), 4, RoundingMode.HALF_UP));
                count++;
            }
        }
        if (count == 0) return BigDecimal.ZERO;
        return totalRate.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
