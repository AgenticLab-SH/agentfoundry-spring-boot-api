package com.skala.agentfoundry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "recommendations",
    uniqueConstraints = @UniqueConstraint(name = "uk_recommendation_engagement", columnNames = "engagement_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "engagement_id", nullable = false)
    private Engagement engagement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private AgentOffering offering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 300)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Recommendation(
        Engagement engagement,
        Member member,
        String comment,
        LocalDateTime createdAt
    ) {
        this.engagement = engagement;
        this.offering = engagement.getOffering();
        this.member = member;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Recommendation create(
        Engagement engagement,
        Member member,
        String comment,
        LocalDateTime createdAt
    ) {
        return new Recommendation(engagement, member, comment, createdAt);
    }
}

