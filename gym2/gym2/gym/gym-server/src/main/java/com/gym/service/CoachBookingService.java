package com.gym.service;

import com.gym.entity.CoachBooking;
import com.gym.vo.CoachBookingVO;

import java.util.List;

public interface CoachBookingService {

    CoachBooking book(Long memberId, Long coachId, String date, String startTime, String endTime, String remark);

    void cancel(Long bookingId, Long memberId);

    void complete(Long bookingId);

    List<CoachBookingVO> getMyBookings(Long memberId);

    List<CoachBookingVO> listAll(String status);
}
