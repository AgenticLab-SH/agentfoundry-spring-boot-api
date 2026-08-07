package com.skala.agentfoundry.service;

import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentOffering;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.EngagementStatus;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import com.skala.agentfoundry.domain.Member;
import com.skala.agentfoundry.domain.OfferingStatus;
import com.skala.agentfoundry.dto.AgentOfferingRequest;
import com.skala.agentfoundry.dto.AgentOfferingResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.AgentOfferingRepository;
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
public class AgentOfferingService {

    private final AgentOfferingRepository offeringRepository;
    private final EngagementRepository engagementRepository;
    private final MemberService memberService;
    private final Clock clock;

    public PageResponse<AgentOfferingResponse> search(
        String keyword,
        ListingType listingType,
        ArtifactType artifactType,
        AgentDomain domain,
        ExecutionEnvironment environment,
        OfferingStatus status,
        Pageable pageable
    ) {
        Page<AgentOfferingResponse> page = offeringRepository.search(
            normalize(keyword), listingType, artifactType, domain, environment, status, pageable
        ).map(AgentOfferingResponse::from);
        return PageResponse.from(page);
    }

    public AgentOfferingResponse get(Long id) {
        return AgentOfferingResponse.from(getEntity(id));
    }

    @Transactional
    public AgentOfferingResponse create(Long memberId, AgentOfferingRequest request) {
        Member provider = memberService.getMemberEntity(memberId);
        AgentOffering offering = AgentOffering.create(provider, request, LocalDateTime.now(clock));
        return AgentOfferingResponse.from(offeringRepository.save(offering));
    }

    @Transactional
    public AgentOfferingResponse update(Long memberId, Long id, AgentOfferingRequest request) {
        AgentOffering offering = getEntity(id);
        requireOwner(offering, memberId);
        try {
            offering.update(request);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.CAPACITY_BELOW_ACTIVE);
        }
        return AgentOfferingResponse.from(offering);
    }

    @Transactional
    public void archive(Long memberId, Long id) {
        AgentOffering offering = getEntity(id);
        requireOwner(offering, memberId);
        if (engagementRepository.existsByOfferingIdAndStatus(id, EngagementStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.ACTIVE_ENGAGEMENT_EXISTS);
        }
        offering.archive();
    }

    public AgentOffering getEntity(Long id) {
        return offeringRepository.findDetailById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND, "Offering을 찾을 수 없습니다."));
    }

    private void requireOwner(AgentOffering offering, Long memberId) {
        if (!offering.getProvider().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}

