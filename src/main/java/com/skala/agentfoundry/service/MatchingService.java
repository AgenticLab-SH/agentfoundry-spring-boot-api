package com.skala.agentfoundry.service;

import com.skala.agentfoundry.domain.AgentOffering;
import com.skala.agentfoundry.domain.AgentRequest;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import com.skala.agentfoundry.domain.OfferingStatus;
import com.skala.agentfoundry.domain.RequestType;
import com.skala.agentfoundry.dto.AgentOfferingResponse;
import com.skala.agentfoundry.dto.MatchResponse;
import com.skala.agentfoundry.dto.MatchResponse.ScoreDetails;
import com.skala.agentfoundry.repository.AgentOfferingRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final AgentOfferingRepository offeringRepository;
    private final AgentRequestService requestService;

    public List<MatchResponse> findMatches(Long memberId, Long requestId) {
        AgentRequest request = requestService.getEntity(requestId);
        if (!request.getRequester().getId().equals(memberId)) {
            throw new com.skala.agentfoundry.exception.ApiException(
                com.skala.agentfoundry.exception.ErrorCode.ACCESS_DENIED
            );
        }
        return offeringRepository.findByStatusAndAvailableSlotsGreaterThan(OfferingStatus.OPEN, 0)
            .stream()
            .filter(offering -> !offering.getProvider().getId().equals(memberId))
            .map(offering -> score(request, offering))
            .sorted(Comparator.comparing(MatchResponse::compatibilityScore).reversed()
                .thenComparing(match -> match.offering().id()))
            .toList();
    }

    public MatchResponse score(AgentRequest request, AgentOffering offering) {
        // 총점만 보여주면 이유를 알 수 없어서 비교 항목별 점수도 같이 계산합니다.
        int type = preferredListing(request.getRequestType()) == offering.getListingType() ? 15 : 0;
        int artifact = request.getDesiredArtifactType() == offering.getArtifactType() ? 10 : 0;
        int domain = request.getDomain() == offering.getDomain() ? 25 : 0;
        int environment = environmentCompatible(request.getEnvironment(), offering.getEnvironment()) ? 10 : 0;
        int memory = offering.getMinimumMemoryGb() <= request.getAvailableMemoryGb() ? 10 : 0;
        int budget = offering.getCreditCost() <= request.getMaxCredits() ? 15 : 0;
        int availability = offering.hasAvailableSlot() ? 10 : 0;
        int trust = Math.min(5, offering.getCompletedCount() + offering.getRecommendationCount());
        ScoreDetails details = new ScoreDetails(
            type + artifact, domain, environment + memory, budget, availability, trust
        );
        return new MatchResponse(AgentOfferingResponse.from(offering), details.total(), details);
    }

    public boolean isCompatible(AgentRequest request, AgentOffering offering) {
        return preferredListing(request.getRequestType()) == offering.getListingType()
            && request.getDesiredArtifactType() == offering.getArtifactType()
            && request.getDomain() == offering.getDomain()
            && environmentCompatible(request.getEnvironment(), offering.getEnvironment())
            && offering.getMinimumMemoryGb() <= request.getAvailableMemoryGb()
            && offering.getEstimatedHours() <= request.getExpectedHours();
    }

    private ListingType preferredListing(RequestType requestType) {
        return switch (requestType) {
            case ADOPT_AGENT -> ListingType.ASSET;
            case BUILD_AGENT -> ListingType.BUILD_SERVICE;
            case LEARN -> ListingType.EDUCATION;
            case REVIEW_PROJECT, BETA_TEST, CO_DEVELOP, LAUNCH_SUPPORT -> ListingType.PROJECT;
        };
    }

    private boolean environmentCompatible(
        ExecutionEnvironment requested,
        ExecutionEnvironment offered
    ) {
        if (requested == ExecutionEnvironment.ANY || offered == ExecutionEnvironment.ANY) {
            return true;
        }
        if (requested == offered) {
            return true;
        }
        return requested == ExecutionEnvironment.HYBRID || offered == ExecutionEnvironment.HYBRID;
    }
}
