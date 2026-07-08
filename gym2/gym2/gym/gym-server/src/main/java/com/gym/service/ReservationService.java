package com.gym.service;

import com.gym.vo.ReservationVO;

import java.util.List;

public interface ReservationService {

    /**
     * 预约课程（乐观锁防超卖 → 满员自动转候补）
     */
    ReservationVO book(Long memberId, Long scheduleId, String source);

    /**
     * 取消预约（检查退课截止时间 → 回退名额 → 候补晋升）
     */
    void cancel(Long reservationId, Long memberId, String reason);

    /**
     * 签到
     */
    void checkin(Long reservationId);

    /**
     * 我的预约列表
     */
    List<ReservationVO> getMyReservations(Long memberId);

    /**
     * 管理端：查询全部预约
     */
    List<ReservationVO> listAll(String status);

    /**
     * 定时任务：标记爽约（未签到 → 扣信用分）
     */
    void markAbsent();
}
