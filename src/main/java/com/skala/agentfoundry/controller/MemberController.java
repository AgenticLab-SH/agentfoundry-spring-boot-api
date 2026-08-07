package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.dto.MemberCreateRequest;
import com.skala.agentfoundry.dto.MemberResponse;
import com.skala.agentfoundry.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> register(
        @Valid @RequestBody MemberCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("회원가입이 완료되었습니다.", memberService.register(request)));
    }
}

