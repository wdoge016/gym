package com.gym.service;

import com.gym.entity.WaitList;

import java.util.List;

public interface WaitListService {

    List<WaitList> getMyWaitList(Long memberId);

    void cancelWait(Long waitId, Long memberId);
}
