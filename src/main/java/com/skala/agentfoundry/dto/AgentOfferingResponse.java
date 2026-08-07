package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentOffering;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import com.skala.agentfoundry.domain.OfferingStatus;
import java.time.LocalDateTime;

public record AgentOfferingResponse(
    Long id, Long providerId, String providerDisplayName, String title, String summary,
    ListingType listingType, ArtifactType artifactType, AgentDomain domain,
    ExecutionEnvironment environment, Integer minimumMemoryGb, Integer estimatedHours,
    Integer creditCost, Integer capacity, Integer availableSlots, String acceptanceCriteria,
    String licenseName, OfferingStatus status, Integer completedCount,
    Integer recommendationCount, LocalDateTime createdAt
) {
    public static AgentOfferingResponse from(AgentOffering offering) {
        return new AgentOfferingResponse(
            offering.getId(), offering.getProvider().getId(), offering.getProvider().getDisplayName(),
            offering.getTitle(), offering.getSummary(), offering.getListingType(), offering.getArtifactType(),
            offering.getDomain(), offering.getEnvironment(), offering.getMinimumMemoryGb(),
            offering.getEstimatedHours(), offering.getCreditCost(), offering.getCapacity(),
            offering.getAvailableSlots(), offering.getAcceptanceCriteria(), offering.getLicenseName(),
            offering.getStatus(), offering.getCompletedCount(), offering.getRecommendationCount(),
            offering.getCreatedAt()
        );
    }
}

