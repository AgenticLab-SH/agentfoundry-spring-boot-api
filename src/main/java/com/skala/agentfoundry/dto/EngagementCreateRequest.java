package com.skala.agentfoundry.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EngagementCreateRequest(
    @NotNull(message = "요청 ID는 필수입니다.") @Positive(message = "요청 ID는 양수여야 합니다.") Long requestId,
    @NotNull(message = "Offering ID는 필수입니다.") @Positive(message = "Offering ID는 양수여야 합니다.") Long offeringId
) {
}

