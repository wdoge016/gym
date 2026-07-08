package com.gym.controller;

import com.gym.common.Constants;
import com.gym.common.Result;
import com.gym.entity.Order;
import com.gym.security.UserContext;
import com.gym.service.CardService;
import com.gym.service.OrderService;
import com.gym.vo.CardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "会员卡管理")
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final OrderService orderService;

    private static final Map<String, BigDecimal> PRICE_MAP = Map.of(
            Constants.CARD_TYPE_MONTH, new BigDecimal("299"),
            Constants.CARD_TYPE_QUARTER, new BigDecimal("799"),
            Constants.CARD_TYPE_YEAR, new BigDecimal("2599")
    );

    @Operation(summary = "我的会员卡（会员端）")
    @GetMapping("/my")
    public Result<List<CardVO>> myCards() {
        Long memberId = UserContext.getUserId();
        return Result.ok(cardService.getMyCards(memberId));
    }

    @Operation(summary = "全部会员卡（管理端）")
    @GetMapping("/all")
    public Result<List<CardVO>> allCards() {
        return Result.ok(cardService.getAllCards());
    }

    @Operation(summary = "购买会员卡 → 生成订单")
    @PostMapping
    public Result<Order> buy(@RequestParam String cardType) {
        Long memberId = UserContext.getUserId();
        BigDecimal amount = PRICE_MAP.getOrDefault(cardType, new BigDecimal("299"));
        return Result.ok(orderService.create(memberId, Constants.ORDER_TYPE_CARD_PURCHASE, amount, cardType));
    }

    @Operation(summary = "续费会员卡 → 生成订单")
    @PostMapping("/{id}/renew")
    public Result<Order> renew(@PathVariable Long id) {
        Long memberId = UserContext.getUserId();
        return Result.ok(orderService.create(memberId, Constants.ORDER_TYPE_RENEWAL, new BigDecimal("299"), String.valueOf(id)));
    }
}
