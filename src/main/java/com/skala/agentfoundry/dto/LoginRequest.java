package com.skala.agentfoundry.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "회원 ID는 필수입니다.") String memberId,
    @NotBlank(message = "비밀번호는 필수입니다.") String password
) {
}

