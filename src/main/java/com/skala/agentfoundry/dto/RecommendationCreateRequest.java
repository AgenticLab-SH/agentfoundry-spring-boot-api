package com.skala.agentfoundry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecommendationCreateRequest(
    @NotBlank(message = "추천 의견은 필수입니다.") @Size(min = 5, max = 300, message = "추천 의견은 5~300자로 입력해 주세요.") String comment
) {
}

