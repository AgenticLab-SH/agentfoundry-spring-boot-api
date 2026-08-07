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
@Table(name = "engagements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Engagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private AgentRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private AgentOffering offering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Member provider;

    @Column(name = "credit_cost", nullable = false)
    private Integer creditCost;

    @Column(name = "compatibility_score", nullable = false)
    private Integer compatibilityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EngagementStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Engagement(
        AgentRequest request,
        AgentOffering offering,
        int compatibilityScore,
        LocalDateTime createdAt
    ) {
        this.request = request;
        this.offering = offering;
        this.requester = request.getRequester();
        this.provider = offering.getProvider();
        this.creditCost = offering.getCreditCost();
        this.compatibilityScore = compatibilityScore;
        this.status = EngagementStatus.ACTIVE;
        this.createdAt = createdAt;
    }

    public static Engagement create(
        AgentRequest request,
        AgentOffering offering,
        int compatibilityScore,
        LocalDateTime createdAt
    ) {
        return new Engagement(request, offering, compatibilityScore, createdAt);
    }

    public void cancel(LocalDateTime canceledAt) {
        requireActive();
        this.status = EngagementStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void complete(LocalDateTime completedAt) {
        requireActive();
        this.status = EngagementStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    private void requireActive() {
        if (status != EngagementStatus.ACTIVE) {
            throw new IllegalStateException("Engagement is not active");
        }
    }
}

