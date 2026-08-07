package com.skala.agentfoundry.service;

import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentRequest;
import com.skala.agentfoundry.domain.EngagementStatus;
import com.skala.agentfoundry.domain.Member;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.domain.RequestType;
import com.skala.agentfoundry.dto.AgentRequestRequest;
import com.skala.agentfoundry.dto.AgentRequestResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.AgentRequestRepository;
import com.skala.agentfoundry.repository.EngagementRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentRequestService {

    private final AgentRequestRepository requestRepository;
    private final EngagementRepository engagementRepository;
    private final MemberService memberService;
    private final Clock clock;

    public PageResponse<AgentRequestResponse> search(
        String keyword,
        RequestType requestType,
        AgentDomain domain,
        RequestStatus status,
        Pageable pageable
    ) {
        Page<AgentRequestResponse> page = requestRepository.search(
            normalize(keyword), requestType, domain, status, pageable
        ).map(AgentRequestResponse::from);
        return PageResponse.from(page);
    }

    public AgentRequestResponse get(Long id) {
        return AgentRequestResponse.from(getEntity(id));
    }

    @Transactional
    public AgentRequestResponse create(Long memberId, AgentRequestRequest request) {
        Member requester = memberService.getMemberEntity(memberId);
        AgentRequest entity = AgentRequest.create(requester, request, LocalDateTime.now(clock));
        return AgentRequestResponse.from(requestRepository.save(entity));
    }

    @Transactional
    public AgentRequestResponse update(Long memberId, Long id, AgentRequestRequest request) {
        AgentRequest entity = getEntity(id);
        requireOwner(entity, memberId);
        if (entity.getStatus() != RequestStatus.OPEN) {
            throw new ApiException(ErrorCode.REQUEST_NOT_OPEN);
        }
        entity.update(request);
        return AgentRequestResponse.from(entity);
    }

    @Transactional
    public void cancel(Long memberId, Long id) {
        AgentRequest entity = getEntity(id);
        requireOwner(entity, memberId);
        if (engagementRepository.existsByRequestIdAndStatus(id, EngagementStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.ACTIVE_ENGAGEMENT_EXISTS);
        }
        if (entity.getStatus() != RequestStatus.OPEN) {
            throw new ApiException(ErrorCode.REQUEST_NOT_OPEN);
        }
        entity.cancel();
    }

    public AgentRequest getEntity(Long id) {
        return requestRepository.findDetailById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND, "Agent 요청을 찾을 수 없습니다."));
    }

    private void requireOwner(AgentRequest request, Long memberId) {
        if (!request.getRequester().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}

