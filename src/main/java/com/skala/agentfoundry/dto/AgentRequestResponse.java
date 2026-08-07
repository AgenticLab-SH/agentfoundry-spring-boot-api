package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentRequest;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.domain.RequestType;
import java.time.LocalDateTime;

public record AgentRequestResponse(
    Long id, Long requesterId, String requesterDisplayName, String title, String goal,
    RequestType requestType, ArtifactType desiredArtifactType, AgentDomain domain,
    ExecutionEnvironment environment, Integer availableMemoryGb, Integer maxCredits,
    Integer expectedHours, String constraints, String acceptanceCriteria, RequestStatus status,
    LocalDateTime createdAt
) {
    public static AgentRequestResponse from(AgentRequest request) {
        return new AgentRequestResponse(
            request.getId(), request.getRequester().getId(), request.getRequester().getDisplayName(),
            request.getTitle(), request.getGoal(), request.getRequestType(), request.getDesiredArtifactType(),
            request.getDomain(), request.getEnvironment(), request.getAvailableMemoryGb(),
            request.getMaxCredits(), request.getExpectedHours(), request.getConstraints(),
            request.getAcceptanceCriteria(), request.getStatus(), request.getCreatedAt()
        );
    }
}

