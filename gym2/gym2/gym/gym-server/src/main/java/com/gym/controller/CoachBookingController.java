package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.CoachBooking;
import com.gym.security.UserContext;
import com.gym.service.CoachBookingService;
import com.gym.vo.CoachBookingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "教练预约")
@RestController
@RequestMapping("/api/v1/coach-bookings")
@RequiredArgsConstructor
public class CoachBookingController {

    private final CoachBookingService coachBookingService;

    @Operation(summary = "会员预约教练")
    @PostMapping
    public Result<CoachBooking> book(@RequestBody Map<String, String> body) {
        Long memberId = UserContext.getUserId();
        return Result.ok(coachBookingService.book(memberId,
                Long.valueOf(body.get("coachId")), body.get("date"),
                body.get("startTime"), body.get("endTime"), body.get("remark")));
    }

    @Operation(summary = "取消教练预约")
    @DeleteMapping("/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        coachBookingService.cancel(id, UserContext.getUserId());
        return Result.ok("取消成功");
    }

    @Operation(summary = "完成预约")
    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id) {
        coachBookingService.complete(id);
        return Result.ok("已完成");
    }

    @Operation(summary = "我的教练预约")
    @GetMapping("/my")
    public Result<List<CoachBookingVO>> myBookings() {
        return Result.ok(coachBookingService.getMyBookings(UserContext.getUserId()));
    }

    @Operation(summary = "全部教练预约（管理端）")
    @GetMapping
    public Result<List<CoachBookingVO>> listAll(@RequestParam(required = false) String status) {
        return Result.ok(coachBookingService.listAll(status));
    }
}
