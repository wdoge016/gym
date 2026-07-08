package com.gym.service;

import com.gym.vo.ChatVO;
import com.gym.vo.RecommendVO;

public interface AiService {

    /**
     * AI课程顾问对话
     */
    ChatVO chat(Long memberId, String message);

    /**
     * AI推荐课程
     */
    RecommendVO recommend(Long memberId);
}
