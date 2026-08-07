package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.Engagement;
import com.skala.agentfoundry.domain.EngagementStatus;
import java.time.LocalDateTime;

public record EngagementResponse(
    Long id, Long requestId, String requestTitle, Long offeringId, String offeringTitle,
    Long requesterId, String requesterDisplayName, Long providerId, String providerDisplayName,
    Integer creditCost, Integer compatibilityScore, EngagementStatus status,
    LocalDateTime createdAt, LocalDateTime canceledAt, LocalDateTime completedAt
) {
    public static EngagementResponse from(Engagement engagement) {
        return new EngagementResponse(
            engagement.getId(), engagement.getRequest().getId(), engagement.getRequest().getTitle(),
            engagement.getOffering().getId(), engagement.getOffering().getTitle(),
            engagement.getRequester().getId(), engagement.getRequester().getDisplayName(),
            engagement.getProvider().getId(), engagement.getProvider().getDisplayName(),
            engagement.getCreditCost(), engagement.getCompatibilityScore(), engagement.getStatus(),
            engagement.getCreatedAt(), engagement.getCanceledAt(), engagement.getCompletedAt()
        );
    }
}

