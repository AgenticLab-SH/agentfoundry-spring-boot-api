package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgentOfferingRequest(
    @NotBlank(message = "제목은 필수입니다.") @Size(min = 3, max = 80, message = "제목은 3~80자로 입력해 주세요.") String title,
    @NotBlank(message = "요약은 필수입니다.") @Size(min = 10, max = 500, message = "요약은 10~500자로 입력해 주세요.") String summary,
    @NotNull(message = "등록 유형은 필수입니다.") ListingType listingType,
    @NotNull(message = "Agent 구성 유형은 필수입니다.") ArtifactType artifactType,
    @NotNull(message = "적용 도메인은 필수입니다.") AgentDomain domain,
    @NotNull(message = "실행 환경은 필수입니다.") ExecutionEnvironment environment,
    @NotNull(message = "최소 메모리는 필수입니다.") @Min(value = 1, message = "최소 메모리는 1GB 이상이어야 합니다.") @Max(value = 256, message = "최소 메모리는 256GB 이하여야 합니다.") Integer minimumMemoryGb,
    @NotNull(message = "예상 시간은 필수입니다.") @Min(value = 1, message = "예상 시간은 1시간 이상이어야 합니다.") @Max(value = 200, message = "예상 시간은 200시간 이하여야 합니다.") Integer estimatedHours,
    @NotNull(message = "크레딧 비용은 필수입니다.") @Min(value = 1, message = "크레딧 비용은 1 이상이어야 합니다.") @Max(value = 50, message = "크레딧 비용은 50 이하여야 합니다.") Integer creditCost,
    @NotNull(message = "제공 가능 인원은 필수입니다.") @Min(value = 1, message = "제공 가능 인원은 1명 이상이어야 합니다.") @Max(value = 100, message = "제공 가능 인원은 100명 이하여야 합니다.") Integer capacity,
    @NotBlank(message = "완료 기준은 필수입니다.") @Size(min = 10, max = 500, message = "완료 기준은 10~500자로 입력해 주세요.") String acceptanceCriteria,
    @NotBlank(message = "라이선스는 필수입니다.") @Size(min = 2, max = 60, message = "라이선스는 2~60자로 입력해 주세요.") String licenseName
) {
}

