package com.gym.service;

import com.gym.vo.CardVO;

import java.util.List;

public interface CardService {

    List<CardVO> getMyCards(Long memberId);

    List<CardVO> getAllCards();

    CardVO buy(Long memberId, String cardType);

    CardVO renew(Long cardId);
}
