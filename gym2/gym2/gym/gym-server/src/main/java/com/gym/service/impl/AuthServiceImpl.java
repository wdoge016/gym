package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.dto.LoginDTO;
import com.gym.dto.RegisterDTO;
import com.gym.entity.Member;
import com.gym.exception.BusinessException;
import com.gym.mapper.MemberMapper;
import com.gym.security.JwtUtil;
import com.gym.service.AuthService;
import com.gym.vo.LoginVO;
import com.gym.vo.MemberVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public AuthServiceImpl(MemberMapper memberMapper, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名唯一
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, dto.getUsername());
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号唯一
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, dto.getPhone());
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("手机号已被注册");
        }

        Member member = new Member();
        member.setUsername(dto.getUsername());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setPhone(dto.getPhone());
        member.setRealName(dto.getRealName());
        member.setGender(dto.getGender());
        member.setCreditScore(Constants.CREDIT_SCORE_DEFAULT);
        member.setStatus(1);

        memberMapper.insert(member);
        log.info("会员注册成功: {}", dto.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUsername, dto.getUsername());
        Member member = memberMapper.selectOne(wrapper);

        if (member == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (member.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新登录时间
        member.setLastLoginTime(LocalDateTime.now());
        memberMapper.updateById(member);

        // 生成Token
        String token = jwtUtil.generateMemberToken(member.getId(), Constants.ROLE_STAFF);

        log.info("会员登录成功: {}", dto.getUsername());

        return LoginVO.builder()
                .userId(member.getId())
                .username(member.getUsername())
                .realName(member.getRealName())
                .token(token)
                .userType(Constants.USER_TYPE_MEMBER)
                .build();
    }

    @Override
    public void logout(String token) {
        if (redisTemplate != null) {
            long remaining = jwtUtil.getRemainingTime(token);
            if (remaining > 0) {
                String key = Constants.REDIS_TOKEN_BLACKLIST + token;
                redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remaining));
            }
        }
        log.info("会员退出登录");
    }

    @Override
    public MemberVO getUserInfo(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("用户不存在");
        }

        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setRealName(member.getRealName());
        vo.setPhone(member.getPhone());
        vo.setGender(member.getGender());
        vo.setCreditScore(member.getCreditScore());
        vo.setAvatarUrl(member.getAvatarUrl());
        vo.setStatus(member.getStatus());
        vo.setLastLoginTime(member.getLastLoginTime());
        vo.setCreateTime(member.getCreateTime());
        return vo;
    }

    @Override
    public void changePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 新密码不能与原密码相同
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberMapper.updateById(member);
        log.info("会员密码修改成功: id={}", memberId);
    }
}
