package com.skala.agentfoundry.controller;

import com.skala.agentfoundry.common.ApiResponse;
import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.common.SessionMember;
import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.domain.RequestType;
import com.skala.agentfoundry.dto.AgentRequestRequest;
import com.skala.agentfoundry.dto.AgentRequestResponse;
import com.skala.agentfoundry.dto.MatchResponse;
import com.skala.agentfoundry.service.AgentRequestService;
import com.skala.agentfoundry.service.MatchingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class AgentRequestController {

    private final AgentRequestService requestService;
    private final MatchingService matchingService;

    @GetMapping
    public ApiResponse<PageResponse<AgentRequestResponse>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) RequestType requestType,
        @RequestParam(required = false) AgentDomain domain,
        @RequestParam(required = false) RequestStatus status,
        @ParameterObject
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(
            "Agent 요청 목록을 조회했습니다.",
            requestService.search(keyword, requestType, domain, status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentRequestResponse> get(@PathVariable Long id) {
        return ApiResponse.success("Agent 요청을 조회했습니다.", requestService.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgentRequestResponse>> create(
        @Valid @RequestBody AgentRequestRequest request,
        HttpSession session
    ) {
        AgentRequestResponse response = requestService.create(SessionMember.require(session), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Agent 요청을 등록했습니다.", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<AgentRequestResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody AgentRequestRequest request,
        HttpSession session
    ) {
        return ApiResponse.success(
            "Agent 요청을 수정했습니다.",
            requestService.update(SessionMember.require(session), id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, HttpSession session) {
        requestService.cancel(SessionMember.require(session), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/matches")
    public ApiResponse<List<MatchResponse>> matches(@PathVariable Long id, HttpSession session) {
        return ApiResponse.success(
            "조건별 매칭 후보를 계산했습니다.",
            matchingService.findMatches(SessionMember.require(session), id)
        );
    }
}
