package com.skala.agentfoundry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credit_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engagement_id")
    private Engagement engagement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CreditTransactionType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 160)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private CreditTransaction(
        Member member,
        Engagement engagement,
        CreditTransactionType type,
        Integer amount,
        Integer balanceAfter,
        String description,
        LocalDateTime createdAt
    ) {
        this.member = member;
        this.engagement = engagement;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static CreditTransaction initial(Member member, int amount, LocalDateTime createdAt) {
        return new CreditTransaction(
            member,
            null,
            CreditTransactionType.INITIAL,
            amount,
            member.getCreditBalance(),
            "초기 데모 크레딧",
            createdAt
        );
    }

    public static CreditTransaction engagementUse(
        Member member,
        Engagement engagement,
        LocalDateTime createdAt
    ) {
        return new CreditTransaction(
            member,
            engagement,
            CreditTransactionType.ENGAGEMENT_USE,
            -engagement.getCreditCost(),
            member.getCreditBalance(),
            "Agent 참여 크레딧 사용",
            createdAt
        );
    }

    public static CreditTransaction cancelRefund(
        Member member,
        Engagement engagement,
        LocalDateTime createdAt
    ) {
        return new CreditTransaction(
            member,
            engagement,
            CreditTransactionType.CANCEL_REFUND,
            engagement.getCreditCost(),
            member.getCreditBalance(),
            "Agent 참여 취소 환급",
            createdAt
        );
    }

    public static CreditTransaction providerReward(
        Member provider,
        Engagement engagement,
        LocalDateTime createdAt
    ) {
        return new CreditTransaction(
            provider,
            engagement,
            CreditTransactionType.PROVIDER_REWARD,
            engagement.getCreditCost(),
            provider.getCreditBalance(),
            "Agent 참여 완료 제공자 보상",
            createdAt
        );
    }
}
