package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.Constants;
import com.gym.entity.MemberCard;
import com.gym.entity.Order;
import com.gym.entity.Payment;
import com.gym.exception.BusinessException;
import com.gym.mapper.MemberCardMapper;
import com.gym.mapper.OrderMapper;
import com.gym.mapper.PaymentMapper;
import com.gym.service.CardService;
import com.gym.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CardService cardService;
    private final MemberCardMapper cardMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long orderId, Long memberId, String payMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Constants.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("订单状态异常，无法支付");
        }

        // 生成幂等键
        String transactionNo = UUID.randomUUID().toString().replace("-", "");

        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPayMethod(payMethod != null ? payMethod : "WECHAT");
        payment.setAmount(order.getAmount());
        payment.setTransactionNo(transactionNo);
        payment.setStatus("SUCCESS");
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        // 更新订单状态为已支付
        order.setStatus(Constants.STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 支付成功后处理购卡/续费
        if (Constants.ORDER_TYPE_CARD_PURCHASE.equals(order.getOrderType())) {
            String cardType = order.getRemark(); // remark 存储卡类型
            cardService.buy(memberId, cardType != null ? cardType : "MONTH");
            log.info("自动开通会员卡: memberId={}, cardType={}", memberId, cardType);
        } else if (Constants.ORDER_TYPE_RENEWAL.equals(order.getOrderType())) {
            Long cardId = Long.valueOf(order.getRemark()); // remark 存储卡ID
            cardService.renew(cardId);
            log.info("续费成功: memberId={}, cardId={}", memberId, cardId);
        }

        log.info("支付成功: orderId={}, transactionNo={}, amount={}", orderId, transactionNo, order.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Constants.STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException("仅已支付订单可退款");
        }

        // 退款时扣减对应会员卡时长
        if (Constants.ORDER_TYPE_CARD_PURCHASE.equals(order.getOrderType())
                || Constants.ORDER_TYPE_RENEWAL.equals(order.getOrderType())) {
            LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberCard::getMemberId, order.getMemberId())
                    .eq(MemberCard::getStatus, 1)
                    .orderByDesc(MemberCard::getEndDate)
                    .last("LIMIT 1");
            MemberCard card = cardMapper.selectOne(wrapper);
            if (card != null && card.getEndDate() != null) {
                String cardType = order.getOrderType().equals(Constants.ORDER_TYPE_CARD_PURCHASE)
                        ? order.getRemark() : null;
                if (cardType == null) cardType = "MONTH";
                card.setEndDate(minusMonths(card.getEndDate(), cardType));
                // 到期日 <= 今天 → 直接删卡（失效）
                if (!card.getEndDate().isAfter(LocalDate.now())) {
                    cardMapper.deleteById(card.getId());
                    log.info("退款后会员卡已到期，删除: cardId={}", card.getId());
                } else {
                    cardMapper.updateById(card);
                }
            }
        }

        order.setStatus(Constants.STATUS_REFUNDED);
        orderMapper.updateById(order);

        log.info("退款成功: orderId={}", orderId);
    }

    private LocalDate minusMonths(LocalDate base, String cardType) {
        return switch (cardType) {
            case Constants.CARD_TYPE_MONTH -> base.minusMonths(1);
            case Constants.CARD_TYPE_QUARTER -> base.minusMonths(3);
            case Constants.CARD_TYPE_YEAR -> base.minusYears(1);
            default -> base.minusMonths(1);
        };
    }
}
