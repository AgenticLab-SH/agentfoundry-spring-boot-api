package com.skala.agentfoundry.service;

import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.domain.AgentOffering;
import com.skala.agentfoundry.domain.AgentRequest;
import com.skala.agentfoundry.domain.CreditTransaction;
import com.skala.agentfoundry.domain.Engagement;
import com.skala.agentfoundry.domain.EngagementStatus;
import com.skala.agentfoundry.domain.Member;
import com.skala.agentfoundry.domain.OfferingStatus;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.dto.EngagementResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.CreditTransactionRepository;
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
public class EngagementService {

    private final EngagementRepository engagementRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final AgentRequestService requestService;
    private final AgentOfferingService offeringService;
    private final MatchingService matchingService;
    private final Clock clock;

    @Transactional
    public EngagementResponse create(Long memberId, Long requestId, Long offeringId) {
        AgentRequest request = requestService.getEntity(requestId);
        AgentOffering offering = offeringService.getEntity(offeringId);
        requireRequester(request, memberId);

        if (request.getRequester().getId().equals(offering.getProvider().getId())) {
            throw new ApiException(ErrorCode.SELF_ENGAGEMENT_NOT_ALLOWED);
        }
        if (request.getStatus() != RequestStatus.OPEN) {
            throw new ApiException(ErrorCode.REQUEST_NOT_OPEN);
        }
        if (offering.getStatus() != OfferingStatus.OPEN) {
            throw new ApiException(ErrorCode.OFFERING_NOT_OPEN);
        }
        if (!offering.hasAvailableSlot()) {
            throw new ApiException(ErrorCode.OFFERING_FULL);
        }
        if (!matchingService.isCompatible(request, offering)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_COMPATIBLE);
        }
        if (offering.getCreditCost() > request.getMaxCredits()) {
            throw new ApiException(ErrorCode.BUDGET_EXCEEDED);
        }
        if (engagementRepository.existsByRequestIdAndStatus(requestId, EngagementStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.ENGAGEMENT_ALREADY_EXISTS);
        }

        Member requester = request.getRequester();
        if (!requester.hasCredits(offering.getCreditCost())) {
            throw new ApiException(ErrorCode.INSUFFICIENT_CREDITS);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        int score = matchingService.score(request, offering).compatibilityScore();
        // 여기부터는 크레딧·슬롯·요청 상태·원장을 한 트랜잭션에서 같이 바꿉니다.
        requester.useCredits(offering.getCreditCost());
        offering.reserveSlot();
        request.markMatched();
        Engagement engagement = engagementRepository.save(Engagement.create(request, offering, score, now));
        creditTransactionRepository.save(CreditTransaction.engagementUse(requester, engagement, now));
        return EngagementResponse.from(engagement);
    }

    public PageResponse<EngagementResponse> getMine(Long memberId, Pageable pageable) {
        Page<EngagementResponse> page = engagementRepository.findMine(memberId, pageable)
            .map(EngagementResponse::from);
        return PageResponse.from(page);
    }

    public EngagementResponse get(Long memberId, Long id) {
        Engagement engagement = getEntity(id);
        requireParticipant(engagement, memberId);
        return EngagementResponse.from(engagement);
    }

    @Transactional
    public EngagementResponse cancel(Long memberId, Long id) {
        Engagement engagement = getEntity(id);
        if (!engagement.getRequester().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        requireActive(engagement);

        LocalDateTime now = LocalDateTime.now(clock);
        // 취소할 때는 참여 생성에서 바뀐 크레딧과 슬롯을 함께 되돌립니다.
        engagement.getRequester().addCredits(engagement.getCreditCost());
        engagement.getOffering().releaseSlot();
        engagement.getRequest().reopen();
        engagement.cancel(now);
        creditTransactionRepository.save(
            CreditTransaction.cancelRefund(engagement.getRequester(), engagement, now)
        );
        return EngagementResponse.from(engagement);
    }

    @Transactional
    public EngagementResponse complete(Long memberId, Long id) {
        Engagement engagement = getEntity(id);
        if (!engagement.getProvider().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        requireActive(engagement);

        LocalDateTime now = LocalDateTime.now(clock);
        engagement.getProvider().addCredits(engagement.getCreditCost());
        engagement.getOffering().completeOne();
        engagement.getRequest().close();
        engagement.complete(now);
        creditTransactionRepository.save(
            CreditTransaction.providerReward(engagement.getProvider(), engagement, now)
        );
        return EngagementResponse.from(engagement);
    }

    public Engagement getEntity(Long id) {
        return engagementRepository.findDetailById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND, "참여 내역을 찾을 수 없습니다."));
    }

    private void requireRequester(AgentRequest request, Long memberId) {
        if (!request.getRequester().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireParticipant(Engagement engagement, Long memberId) {
        if (!engagement.getRequester().getId().equals(memberId)
            && !engagement.getProvider().getId().equals(memberId)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireActive(Engagement engagement) {
        if (engagement.getStatus() != EngagementStatus.ACTIVE) {
            throw new ApiException(ErrorCode.INVALID_ENGAGEMENT_STATUS);
        }
    }
}
