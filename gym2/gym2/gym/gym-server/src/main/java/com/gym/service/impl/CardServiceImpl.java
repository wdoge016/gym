package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.entity.Member;
import com.gym.entity.MemberCard;
import com.gym.exception.BusinessException;
import com.gym.mapper.MemberCardMapper;
import com.gym.mapper.MemberMapper;
import com.gym.service.CardService;
import com.gym.vo.CardVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final MemberCardMapper cardMapper;
    private final MemberMapper memberMapper;

    private static final Map<String, BigDecimal> PRICE_MAP = Map.of(
            Constants.CARD_TYPE_MONTH, new BigDecimal("299"),
            Constants.CARD_TYPE_QUARTER, new BigDecimal("799"),
            Constants.CARD_TYPE_YEAR, new BigDecimal("2599")
    );

    @Override
    public List<CardVO> getAllCards() {
        return cardMapper.selectList(null).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardVO> getMyCards(Long memberId) {
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getMemberId, memberId)
                .orderByDesc(MemberCard::getCreateTime);
        return cardMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public CardVO buy(Long memberId, String cardType) {
        // 查找该会员当前生效的任意卡（按到期日最远优先）
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getMemberId, memberId)
                .eq(MemberCard::getStatus, 1)
                .orderByDesc(MemberCard::getEndDate)
                .last("LIMIT 1");
        MemberCard existCard = cardMapper.selectOne(wrapper);

        if (existCard != null) {
            // 合并到现有卡上，延长到期日
            LocalDate base = existCard.getEndDate() != null && existCard.getEndDate().isAfter(LocalDate.now())
                    ? existCard.getEndDate() : LocalDate.now();
            existCard.setEndDate(plusMonths(base, cardType));
            existCard.setAmount(existCard.getAmount().add(PRICE_MAP.getOrDefault(cardType, new BigDecimal("299"))));
            // 保留级别更高的卡类型（年>季>月）
            if (isHigherTier(cardType, existCard.getCardType())) {
                existCard.setCardType(cardType);
            }
            cardMapper.updateById(existCard);
            return toVO(existCard);
        }

        // 无有效卡，新建
        MemberCard card = new MemberCard();
        card.setMemberId(memberId);
        card.setCardType(cardType);
        card.setAmount(PRICE_MAP.getOrDefault(cardType, new BigDecimal("299")));
        card.setStartDate(LocalDate.now());
        card.setEndDate(plusMonths(LocalDate.now(), cardType));
        card.setRemainingTimes(-1);
        card.setStatus(1);
        cardMapper.insert(card);
        return toVO(card);
    }

    private boolean isHigherTier(String newType, String oldType) {
        int n = switch (newType) { case Constants.CARD_TYPE_YEAR -> 3; case Constants.CARD_TYPE_QUARTER -> 2; default -> 1; };
        int o = switch (oldType) { case Constants.CARD_TYPE_YEAR -> 3; case Constants.CARD_TYPE_QUARTER -> 2; default -> 1; };
        return n > o;
    }

    private LocalDate plusMonths(LocalDate base, String cardType) {
        return switch (cardType) {
            case Constants.CARD_TYPE_MONTH -> base.plusMonths(1);
            case Constants.CARD_TYPE_QUARTER -> base.plusMonths(3);
            case Constants.CARD_TYPE_YEAR -> base.plusYears(1);
            default -> throw new BusinessException("不支持的会员卡类型: " + cardType);
        };
    }

    @Override
    public CardVO renew(Long cardId) {
        MemberCard card = cardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("会员卡不存在");
        }

        if (card.getEndDate() != null && card.getEndDate().isAfter(LocalDate.now())) {
            card.setEndDate(card.getEndDate().plusMonths(1));
        } else {
            card.setEndDate(LocalDate.now().plusMonths(1));
        }
        card.setStatus(1);
        cardMapper.updateById(card);
        return toVO(card);
    }

    private CardVO toVO(MemberCard card) {
        CardVO vo = new CardVO();
        vo.setId(card.getId());
        vo.setMemberId(card.getMemberId());
        vo.setCardType(card.getCardType());
        vo.setTotalTimes(card.getTotalTimes());
        vo.setRemainingTimes(card.getRemainingTimes());
        vo.setStartDate(card.getStartDate());
        vo.setEndDate(card.getEndDate());
        vo.setAmount(card.getAmount());
        vo.setStatus(card.getStatus());
        vo.setCreateTime(card.getCreateTime());

        Member member = memberMapper.selectById(card.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
        }
        return vo;
    }
}
