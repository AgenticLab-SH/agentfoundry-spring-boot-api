package com.skala.agentfoundry.dto;

public record MatchResponse(
    AgentOfferingResponse offering,
    Integer compatibilityScore,
    ScoreDetails scoreDetails
) {
    public record ScoreDetails(
        Integer typeAndArtifact, Integer domain, Integer resource,
        Integer budget, Integer availability, Integer trust
    ) {
        public int total() {
            return typeAndArtifact + domain + resource + budget + availability + trust;
        }
    }
}

