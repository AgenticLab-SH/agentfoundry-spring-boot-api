package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.common.SessionMember;
import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import com.skala.agentfoundry.domain.OfferingStatus;
import com.skala.agentfoundry.dto.AgentOfferingRequest;
import com.skala.agentfoundry.dto.AgentOfferingResponse;
import com.skala.agentfoundry.dto.RecommendationResponse;
import com.skala.agentfoundry.service.AgentOfferingService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class AgentOfferingController {

    private final AgentOfferingService offeringService;
    private final RecommendationService recommendationService;

    @GetMapping
    public ApiResponse<PageResponse<AgentOfferingResponse>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) ListingType listingType,
        @RequestParam(required = false) ArtifactType artifactType,
        @RequestParam(required = false) AgentDomain domain,
        @RequestParam(required = false) ExecutionEnvironment environment,
        @RequestParam(required = false) OfferingStatus status,
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(
            "Agent Offering 목록을 조회했습니다.",
            offeringService.search(keyword, listingType, artifactType, domain, environment, status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentOfferingResponse> get(@PathVariable Long id) {
        return ApiResponse.success("Agent Offering을 조회했습니다.", offeringService.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgentOfferingResponse>> create(
        @Valid @RequestBody AgentOfferingRequest request,
        HttpSession session
    ) {
        AgentOfferingResponse response = offeringService.create(SessionMember.require(session), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Agent Offering을 등록했습니다.", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<AgentOfferingResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody AgentOfferingRequest request,
        HttpSession session
    ) {
        return ApiResponse.success(
            "Agent Offering을 수정했습니다.",
            offeringService.update(SessionMember.require(session), id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable Long id, HttpSession session) {
        offeringService.archive(SessionMember.require(session), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/recommendations")
    public ApiResponse<PageResponse<RecommendationResponse>> recommendations(
        @PathVariable Long id,
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        offeringService.get(id);
        return ApiResponse.success(
            "추천 목록을 조회했습니다.",
            recommendationService.getByOffering(id, pageable)
        );
    }
}
