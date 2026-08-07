package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.RequestType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgentRequestRequest(
    @NotBlank(message = "요청 제목은 필수입니다.") @Size(min = 3, max = 80, message = "요청 제목은 3~80자로 입력해 주세요.") String title,
    @NotBlank(message = "요청 목표는 필수입니다.") @Size(min = 10, max = 500, message = "요청 목표는 10~500자로 입력해 주세요.") String goal,
    @NotNull(message = "요청 유형은 필수입니다.") RequestType requestType,
    @NotNull(message = "필요한 Agent 구성 유형은 필수입니다.") ArtifactType desiredArtifactType,
    @NotNull(message = "적용 도메인은 필수입니다.") AgentDomain domain,
    @NotNull(message = "실행 환경은 필수입니다.") ExecutionEnvironment environment,
    @NotNull(message = "사용 가능 메모리는 필수입니다.") @Min(value = 1, message = "사용 가능 메모리는 1GB 이상이어야 합니다.") @Max(value = 256, message = "사용 가능 메모리는 256GB 이하여야 합니다.") Integer availableMemoryGb,
    @NotNull(message = "최대 크레딧은 필수입니다.") @Min(value = 1, message = "최대 크레딧은 1 이상이어야 합니다.") @Max(value = 50, message = "최대 크레딧은 50 이하여야 합니다.") Integer maxCredits,
    @NotNull(message = "예상 투입 시간은 필수입니다.") @Min(value = 1, message = "예상 투입 시간은 1시간 이상이어야 합니다.") @Max(value = 200, message = "예상 투입 시간은 200시간 이하여야 합니다.") Integer expectedHours,
    @NotBlank(message = "제약 조건은 필수입니다.") @Size(min = 5, max = 500, message = "제약 조건은 5~500자로 입력해 주세요.") String constraints,
    @NotBlank(message = "완료 기준은 필수입니다.") @Size(min = 10, max = 500, message = "완료 기준은 10~500자로 입력해 주세요.") String acceptanceCriteria
) {
}

