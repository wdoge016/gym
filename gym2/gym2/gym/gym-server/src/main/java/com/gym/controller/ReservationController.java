package com.gym.controller;

import com.gym.common.Result;
import com.gym.dto.ReservationDTO;
import com.gym.security.UserContext;
import com.gym.service.ReservationService;
import com.gym.vo.ReservationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "预约管理")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "预约课程")
    @PostMapping
    public Result<ReservationVO> book(@RequestBody ReservationDTO dto) {
        Long memberId = UserContext.getUserId();
        return Result.ok(reservationService.book(memberId, dto.getScheduleId(), "MANUAL"));
    }

    @Operation(summary = "取消预约")
    @DeleteMapping("/{id}")
    public Result<?> cancel(@PathVariable Long id, @RequestParam(required = false, defaultValue = "用户取消") String reason) {
        Long memberId = UserContext.getUserId();
        reservationService.cancel(id, memberId, reason);
        return Result.ok("取消成功");
    }

    @Operation(summary = "签到")
    @PutMapping("/{id}/checkin")
    public Result<?> checkin(@PathVariable Long id) {
        reservationService.checkin(id);
        return Result.ok("签到成功");
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/my")
    public Result<List<ReservationVO>> myReservations() {
        Long memberId = UserContext.getUserId();
        return Result.ok(reservationService.getMyReservations(memberId));
    }

    @Operation(summary = "管理端：查询全部预约")
    @GetMapping("/all")
    public Result<List<ReservationVO>> listAll(@RequestParam(required = false) String status) {
        return Result.ok(reservationService.listAll(status));
    }

    @Operation(summary = "标记爽约（管理端/定时任务）")
    @PostMapping("/mark-absent")
    public Result<?> markAbsent() {
        reservationService.markAbsent();
        return Result.ok("标记完成");
    }
}
