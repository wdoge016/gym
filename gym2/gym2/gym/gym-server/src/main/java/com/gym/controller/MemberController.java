package com.gym.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.dto.MemberDTO;
import com.gym.entity.Member;
import com.gym.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "会员管理")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "分页查询会员列表")
    @GetMapping
    public Result<PageResult<Member>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        IPage<Member> page = memberService.page(current, size, keyword);
        return Result.ok(PageResult.of(page));
    }

    @Operation(summary = "根据用户名查询会员")
    @GetMapping("/username/{username}")
    public Result<Member> getByUsername(@PathVariable String username) {
        // service内部已完成用户名非空校验、不存在抛异常，controller无需额外判断
        Member member = memberService.getByUsername(username);
        return Result.ok(member);
    }

    @Operation(summary = "新增会员")
    @PostMapping
    public Result<?> add(@RequestBody MemberDTO dto) {
        memberService.add(dto);
        return Result.ok("新增成功");
    }

    @Operation(summary = "获取会员详情")
    @GetMapping("/{id}")
    public Result<Member> detail(@PathVariable Long id) {
        return Result.ok(memberService.getById(id));
    }

    @Operation(summary = "更新会员信息")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MemberDTO dto) {
        memberService.update(id, dto);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除会员（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        memberService.delete(id);
        return Result.ok("删除成功");
    }

    @Operation(summary = "更新会员状态（启用/禁用）")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        memberService.updateStatus(id, status);
        return Result.ok("状态更新成功");
    }
}