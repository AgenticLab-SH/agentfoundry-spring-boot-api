package com.skala.agentfoundry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
    @NotBlank(message = "회원 ID는 필수입니다.")
    @Pattern(regexp = "^[a-z0-9_]{4,20}$", message = "회원 ID는 영문 소문자, 숫자, 밑줄 4~20자여야 합니다.")
    String memberId,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 50, message = "비밀번호는 6~50자여야 합니다.")
    String password,

    @NotBlank(message = "표시 이름은 필수입니다.")
    @Size(max = 30, message = "표시 이름은 30자 이하여야 합니다.")
    String displayName
) {
}

