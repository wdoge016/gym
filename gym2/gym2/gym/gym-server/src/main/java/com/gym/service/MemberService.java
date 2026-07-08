package com.gym.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gym.dto.MemberDTO;
import com.gym.entity.Member;

public interface MemberService {

    IPage<Member> page(Integer current, Integer size, String keyword);

    Member getById(Long id);

    void add(MemberDTO dto);

    void update(Long id, MemberDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    Member getByUsername(String username);
}
