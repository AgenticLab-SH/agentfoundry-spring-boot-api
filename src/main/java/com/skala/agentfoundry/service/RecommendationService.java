package com.skala.agentfoundry.service;

import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.domain.Engagement;
import com.skala.agentfoundry.domain.EngagementStatus;
import com.skala.agentfoundry.domain.Recommendation;
import com.skala.agentfoundry.dto.RecommendationResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.RecommendationRepository;
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
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final EngagementService engagementService;
    private final Clock clock;

    @Transactional
    public RecommendationResponse create(Long memberId, Long engagementId, String comment) {
        Engagement engagement = engagementService.getEntity(engagementId);
        if (!engagement.getRequester().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        if (engagement.getRequester().getId().equals(engagement.getProvider().getId())) {
            throw new ApiException(ErrorCode.SELF_ENGAGEMENT_NOT_ALLOWED);
        }
        if (engagement.getStatus() != EngagementStatus.COMPLETED) {
            throw new ApiException(ErrorCode.INVALID_ENGAGEMENT_STATUS, "완료된 참여만 추천할 수 있습니다.");
        }
        if (recommendationRepository.existsByEngagementId(engagementId)) {
            throw new ApiException(ErrorCode.RECOMMENDATION_ALREADY_EXISTS);
        }

        Recommendation recommendation = recommendationRepository.save(Recommendation.create(
            engagement,
            engagement.getRequester(),
            comment,
            LocalDateTime.now(clock)
        ));
        engagement.getOffering().addRecommendation();
        return RecommendationResponse.from(recommendation);
    }

    public PageResponse<RecommendationResponse> getByOffering(Long offeringId, Pageable pageable) {
        Page<RecommendationResponse> page = recommendationRepository.findByOfferingId(offeringId, pageable)
            .map(RecommendationResponse::from);
        return PageResponse.from(page);
    }
}

