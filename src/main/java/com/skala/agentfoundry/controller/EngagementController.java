package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.common.SessionMember;
import com.skala.agentfoundry.dto.EngagementCreateRequest;
import com.skala.agentfoundry.dto.EngagementResponse;
import com.skala.agentfoundry.dto.RecommendationCreateRequest;
import com.skala.agentfoundry.dto.RecommendationResponse;
import com.skala.agentfoundry.service.EngagementService;
import com.skala.agentfoundry.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engagements")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService engagementService;
    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<ApiResponse<EngagementResponse>> create(
        @Valid @RequestBody EngagementCreateRequest request,
        HttpSession session
    ) {
        EngagementResponse response = engagementService.create(
            SessionMember.require(session), request.requestId(), request.offeringId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Agent 참여를 시작했습니다.", response));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<EngagementResponse>> getMine(
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        HttpSession session
    ) {
        return ApiResponse.success(
            "내 참여 목록을 조회했습니다.",
            engagementService.getMine(SessionMember.require(session), pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<EngagementResponse> get(@PathVariable Long id, HttpSession session) {
        return ApiResponse.success(
            "참여 내역을 조회했습니다.",
            engagementService.get(SessionMember.require(session), id)
        );
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<EngagementResponse> cancel(@PathVariable Long id, HttpSession session) {
        return ApiResponse.success(
            "참여를 취소하고 크레딧을 환급했습니다.",
            engagementService.cancel(SessionMember.require(session), id)
        );
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<EngagementResponse> complete(@PathVariable Long id, HttpSession session) {
        return ApiResponse.success(
            "참여를 완료하고 제공자에게 크레딧을 지급했습니다.",
            engagementService.complete(SessionMember.require(session), id)
        );
    }

    @PostMapping("/{id}/recommendation")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommend(
        @PathVariable Long id,
        @Valid @RequestBody RecommendationCreateRequest request,
        HttpSession session
    ) {
        RecommendationResponse response = recommendationService.create(
            SessionMember.require(session), id, request.comment()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("완료한 Offering을 추천했습니다.", response));
    }
}
