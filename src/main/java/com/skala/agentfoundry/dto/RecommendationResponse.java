package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.Recommendation;
import java.time.LocalDateTime;

public record RecommendationResponse(
    Long id, Long engagementId, Long offeringId, String memberDisplayName,
    String comment, LocalDateTime createdAt
) {
    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
            recommendation.getId(), recommendation.getEngagement().getId(),
            recommendation.getOffering().getId(), recommendation.getMember().getDisplayName(),
            recommendation.getComment(), recommendation.getCreatedAt()
        );
    }
}

