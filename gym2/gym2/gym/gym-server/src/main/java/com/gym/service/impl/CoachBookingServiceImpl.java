package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.entity.Coach;
import com.gym.entity.CoachBooking;
import com.gym.entity.Member;
import com.gym.exception.BusinessException;
import com.gym.mapper.CoachBookingMapper;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.MemberMapper;
import com.gym.service.CoachBookingService;
import com.gym.vo.CoachBookingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachBookingServiceImpl implements CoachBookingService {

    private final CoachBookingMapper coachBookingMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachBooking book(Long memberId, Long coachId, String date, String startTime, String endTime, String remark) {
        // 检查教练是否存在且在职
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null || coach.getStatus() == 0) {
            throw new BusinessException("教练不可用");
        }

        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        // 只能约未来
        if (bookingDate.isBefore(LocalDate.now()) ||
                (bookingDate.equals(LocalDate.now()) && start.isBefore(LocalTime.now()))) {
            throw new BusinessException("只能预约未来时间");
        }

        // 检查该教练该时段是否已被预约
        LambdaQueryWrapper<CoachBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachBooking::getCoachId, coachId)
                .eq(CoachBooking::getAppointmentDate, bookingDate)
                .eq(CoachBooking::getStatus, "BOOKED")
                .and(w -> w
                        .lt(CoachBooking::getStartTime, end)
                        .gt(CoachBooking::getEndTime, start));
        if (coachBookingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该时段已被预约");
        }

        CoachBooking booking = new CoachBooking();
        booking.setMemberId(memberId);
        booking.setCoachId(coachId);
        booking.setAppointmentDate(bookingDate);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus("BOOKED");
        booking.setRemark(remark);
        coachBookingMapper.insert(booking);
        return booking;
    }

    @Override
    public void cancel(Long bookingId, Long memberId) {
        CoachBooking booking = coachBookingMapper.selectById(bookingId);
        if (booking == null || !booking.getMemberId().equals(memberId)) {
            throw new BusinessException("无权取消");
        }
        if (!"BOOKED".equals(booking.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        booking.setStatus("CANCELLED");
        coachBookingMapper.updateById(booking);
    }

    @Override
    public void complete(Long bookingId) {
        CoachBooking booking = coachBookingMapper.selectById(bookingId);
        if (booking == null) throw new BusinessException("预约不存在");
        booking.setStatus("COMPLETED");
        coachBookingMapper.updateById(booking);
    }

    @Override
    public List<CoachBookingVO> getMyBookings(Long memberId) {
        LambdaQueryWrapper<CoachBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachBooking::getMemberId, memberId)
                .orderByDesc(CoachBooking::getAppointmentDate)
                .orderByDesc(CoachBooking::getStartTime);
        return coachBookingMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBookingVO> listAll(String status) {
        LambdaQueryWrapper<CoachBooking> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(CoachBooking::getStatus, status);
        wrapper.orderByDesc(CoachBooking::getAppointmentDate);
        return coachBookingMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private CoachBookingVO toVO(CoachBooking b) {
        CoachBookingVO vo = new CoachBookingVO();
        vo.setId(b.getId());
        vo.setMemberId(b.getMemberId());
        vo.setCoachId(b.getCoachId());
        vo.setAppointmentDate(b.getAppointmentDate());
        vo.setStartTime(b.getStartTime());
        vo.setEndTime(b.getEndTime());
        vo.setStatus(b.getStatus());
        vo.setRemark(b.getRemark());

        Coach coach = coachMapper.selectById(b.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getName());
            vo.setCoachSpeciality(coach.getSpeciality());
        }

        Member member = memberMapper.selectById(b.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
        }

        return vo;
    }
}
