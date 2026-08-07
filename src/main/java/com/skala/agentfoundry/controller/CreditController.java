package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.common.SessionMember;
import com.skala.agentfoundry.dto.CreditTransactionResponse;
import com.skala.agentfoundry.dto.MemberResponse;
import com.skala.agentfoundry.service.CreditService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credits/me")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping
    public ApiResponse<MemberResponse> getBalance(HttpSession session) {
        return ApiResponse.success(
            "크레딧 잔액을 조회했습니다.",
            creditService.getBalance(SessionMember.require(session))
        );
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<CreditTransactionResponse>> getTransactions(
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
        HttpSession session
    ) {
        return ApiResponse.success(
            "크레딧 원장을 조회했습니다.",
            creditService.getTransactions(SessionMember.require(session), pageable)
        );
    }
}
