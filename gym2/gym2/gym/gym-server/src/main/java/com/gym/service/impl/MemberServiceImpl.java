package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.MemberDTO;
import com.gym.entity.Member;
import com.gym.exception.BusinessException;
import com.gym.mapper.MemberMapper;
import com.gym.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void add(MemberDTO dto) {
        Member member = new Member();
        member.setUsername(dto.getUsername());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setRealName(dto.getRealName());
        member.setPhone(dto.getPhone());
        member.setGender(dto.getGender());
        member.setCreditScore(100);
        member.setStatus(1);
        memberMapper.insert(member);
    }

    @Override
    public IPage<Member> page(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Member::getUsername, keyword)
                    .or()
                    .like(Member::getRealName, keyword)
                    .or()
                    .like(Member::getPhone, keyword);
        }
        wrapper.orderByDesc(Member::getCreateTime);
        return memberMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Member getById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        return member;
    }

    @Override
    public Member getByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new BusinessException("用户名不能为空");
        }
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, username);
        // 过滤逻辑删除的会员
        wrapper.eq(Member::getDeleted, 0);
        Member member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("该用户名对应的会员不存在");
        }
        return member;
    }

    @Override
    public void update(Long id, MemberDTO dto) {
        Member member = getById(id);
        if (StringUtils.hasText(dto.getPhone())) {
            member.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            member.setRealName(dto.getRealName());
        }
        if (dto.getGender() != null) {
            member.setGender(dto.getGender());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getCreditScore() != null) {
            member.setCreditScore(dto.getCreditScore());
        }
        memberMapper.updateById(member);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Member member = getById(id);
        member.setStatus(status);
        memberMapper.updateById(member);
    }

    @Override
    public void delete(Long id) {
        memberMapper.deleteById(id);
    }
}
