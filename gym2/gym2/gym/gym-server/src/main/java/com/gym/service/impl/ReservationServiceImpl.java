package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.entity.*;
import com.gym.exception.BusinessException;
import com.gym.mapper.*;
import com.gym.service.ReservationService;
import com.gym.service.SystemConfigService;
import com.gym.vo.ReservationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final ScheduleMapper scheduleMapper;
    private final MemberMapper memberMapper;
    private final WaitListMapper waitListMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final SystemConfigService systemConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationVO book(Long memberId, Long scheduleId, String source) {
        // 1. 检查会员状态
        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getStatus() == 0) {
            throw new BusinessException("会员不存在或已禁用");
        }

        // 2. 信用分检查（读取动态配置，默认60）
        int minScore = getConfigInt("credit_score_min", 60);
        if (member.getCreditScore() != null && member.getCreditScore() < minScore) {
            throw new BusinessException("信用分不足(" + member.getCreditScore() + "/" + minScore + ")，暂无法预约。请连续签到恢复信用分。");
        }

        // 3. 检查排课是否存在
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getStatus() == 0) {
            throw new BusinessException("该排课不存在或已取消");
        }

        // 4. 检查排课是否已开始
        if (schedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该课程已经开始，无法预约");
        }

        // 5. 检查是否已预约
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getMemberId, memberId)
                .eq(Reservation::getScheduleId, scheduleId)
                .eq(Reservation::getStatus, Constants.RES_STATUS_BOOKED);
        if (reservationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已预约该课程，请勿重复预约");
        }

        // 6. 乐观锁扣名额
        int affected = scheduleMapper.incrementBookedCount(scheduleId);
        if (affected == 0) {
            // 满员 → 自动转候补
            log.info("排课已满员，会员 {} 自动加入候补: scheduleId={}", memberId, scheduleId);
            WaitList wait = new WaitList();
            wait.setMemberId(memberId);
            wait.setScheduleId(scheduleId);
            wait.setStatus("WAITING");
            waitListMapper.insert(wait);

            throw new BusinessException("该课程已满员，已自动为您加入候补队列，有人取消时将自动通知您。");
        }

        // 7. 创建预约
        Reservation reservation = new Reservation();
        reservation.setMemberId(memberId);
        reservation.setScheduleId(scheduleId);
        reservation.setSource(source != null ? source : Constants.SOURCE_MANUAL);
        reservation.setStatus(Constants.RES_STATUS_BOOKED);
        reservation.setBookTime(LocalDateTime.now());
        reservationMapper.insert(reservation);

        log.info("预约成功: memberId={}, scheduleId={}, reservationId={}, source={}",
                memberId, scheduleId, reservation.getId(), source);

        return toVO(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId, Long memberId, String reason) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!reservation.getMemberId().equals(memberId)) {
            throw new BusinessException("无权取消他人预约");
        }
        if (!Constants.RES_STATUS_BOOKED.equals(reservation.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }

        // 检查退课截止时间
        Schedule schedule = scheduleMapper.selectById(reservation.getScheduleId());
        if (schedule != null && schedule.getCancelDeadline() != null) {
            LocalDateTime deadline = schedule.getStartTime().minusMinutes(schedule.getCancelDeadline());
            if (LocalDateTime.now().isAfter(deadline)) {
                throw new BusinessException("已超过退课截止时间（课前" + schedule.getCancelDeadline() + "分钟），无法取消");
            }
        }

        // 更新预约状态
        reservation.setStatus(Constants.RES_STATUS_CANCELLED);
        reservation.setCancelTime(LocalDateTime.now());
        reservation.setCancelReason(reason);
        reservationMapper.updateById(reservation);

        // 回退名额
        scheduleMapper.decrementBookedCount(reservation.getScheduleId());

        // 候补晋升：查询最早候补，自动转为预约
        WaitList wait = waitListMapper.selectEarliestWaiting(reservation.getScheduleId());
        if (wait != null) {
            // 乐观锁扣名额（为候补预留）
            int affected = scheduleMapper.incrementBookedCount(reservation.getScheduleId());
            if (affected > 0) {
                // 转为预约
                Reservation newRes = new Reservation();
                newRes.setMemberId(wait.getMemberId());
                newRes.setScheduleId(wait.getScheduleId());
                newRes.setSource(Constants.SOURCE_WAITLIST);
                newRes.setStatus(Constants.RES_STATUS_BOOKED);
                newRes.setBookTime(LocalDateTime.now());
                reservationMapper.insert(newRes);

                // 更新候补状态
                wait.setStatus("PROMOTED");
                waitListMapper.updateById(wait);

                log.info("候补晋升成功: waitId={}, memberId={}, newReservationId={}",
                        wait.getId(), wait.getMemberId(), newRes.getId());
            }
        }

        log.info("取消预约成功: reservationId={}, memberId={}", reservationId, memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!Constants.RES_STATUS_BOOKED.equals(reservation.getStatus())) {
            throw new BusinessException("当前状态不可签到");
        }

        reservation.setStatus(Constants.RES_STATUS_CHECKED_IN);
        reservation.setCheckInTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        log.info("签到成功: reservationId={}", reservationId);
    }

    @Override
    public List<ReservationVO> listAll(String status) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Reservation::getStatus, status);
        }
        wrapper.orderByDesc(Reservation::getCreateTime);
        return reservationMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationVO> getMyReservations(Long memberId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getMemberId, memberId)
                .orderByDesc(Reservation::getCreateTime);
        return reservationMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAbsent() {
        // 查找已过开始时间但状态仍为 BOOKED 的预约
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStatus, Constants.RES_STATUS_BOOKED);
        List<Reservation> list = reservationMapper.selectList(wrapper);

        for (Reservation res : list) {
            Schedule schedule = scheduleMapper.selectById(res.getScheduleId());
            if (schedule != null && schedule.getStartTime().isBefore(LocalDateTime.now().minusMinutes(30))) {
                // 标记爽约
                res.setStatus(Constants.RES_STATUS_ABSENT);
                reservationMapper.updateById(res);

                // 扣信用分
                Member member = memberMapper.selectById(res.getMemberId());
                if (member != null) {
                    int penalty = getConfigInt("credit_score_penalty", 10);
                    int newScore = Math.max(0,
                            (member.getCreditScore() != null ? member.getCreditScore() : 100) - penalty);
                    member.setCreditScore(newScore);
                    memberMapper.updateById(member);
                    log.info("会员 {} 爽约，信用分扣除10分，当前: {}", member.getId(), newScore);
                }
            }
        }
    }

    private ReservationVO toVO(Reservation res) {
        ReservationVO vo = new ReservationVO();
        vo.setId(res.getId());
        vo.setMemberId(res.getMemberId());
        vo.setScheduleId(res.getScheduleId());
        vo.setSource(res.getSource());
        vo.setStatus(res.getStatus());
        vo.setBookTime(res.getBookTime());
        vo.setCheckInTime(res.getCheckInTime());

        // 填充会员名
        Member member = memberMapper.selectById(res.getMemberId());
        if (member != null) vo.setMemberName(member.getRealName());

        // 填充排课信息
        Schedule schedule = scheduleMapper.selectById(res.getScheduleId());
        if (schedule != null) {
            vo.setRoomName(schedule.getRoomName());
            vo.setStartTime(schedule.getStartTime());
            vo.setEndTime(schedule.getEndTime());

            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course != null) vo.setCourseName(course.getName());
            else vo.setCourseName("课程已删除");

            Coach coach = coachMapper.selectById(schedule.getCoachId());
            if (coach != null) vo.setCoachName(coach.getName());
            else vo.setCoachName("教练已删除");
        } else {
            vo.setCourseName("排课已过期");
            vo.setCoachName("-");
            vo.setRoomName("-");
        }

        return vo;
    }

    /** 从系统配置读取整数值，不存在则返回默认值 */
    private int getConfigInt(String key, int defaultValue) {
        try {
            String val = systemConfigService.getAllAsMap().get(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
