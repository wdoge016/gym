package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.WaitList;
import com.gym.exception.BusinessException;
import com.gym.mapper.WaitListMapper;
import com.gym.service.WaitListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitListServiceImpl implements WaitListService {

    private final WaitListMapper waitListMapper;

    @Override
    public List<WaitList> getMyWaitList(Long memberId) {
        LambdaQueryWrapper<WaitList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaitList::getMemberId, memberId)
                .eq(WaitList::getStatus, "WAITING")
                .orderByAsc(WaitList::getCreateTime);
        return waitListMapper.selectList(wrapper);
    }

    @Override
    public void cancelWait(Long waitId, Long memberId) {
        WaitList wait = waitListMapper.selectById(waitId);
        if (wait == null) {
            throw new BusinessException("候补记录不存在");
        }
        if (!wait.getMemberId().equals(memberId)) {
            throw new BusinessException("无权操作他人候补");
        }
        wait.setStatus("CANCELLED");
        waitListMapper.updateById(wait);
    }
}
