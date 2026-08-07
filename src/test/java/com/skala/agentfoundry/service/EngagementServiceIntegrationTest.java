package com.skala.agentfoundry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.EngagementStatus;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.domain.RequestType;
import com.skala.agentfoundry.dto.AgentRequestRequest;
import com.skala.agentfoundry.dto.AgentRequestResponse;
import com.skala.agentfoundry.dto.EngagementResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.AgentOfferingRepository;
import com.skala.agentfoundry.repository.AgentRequestRepository;
import com.skala.agentfoundry.repository.CreditTransactionRepository;
import com.skala.agentfoundry.repository.EngagementRepository;
import com.skala.agentfoundry.repository.MemberRepository;
import com.skala.agentfoundry.repository.RecommendationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EngagementServiceIntegrationTest {

    @Autowired EngagementService engagementService;
    @Autowired RecommendationService recommendationService;
    @Autowired AgentRequestService requestService;
    @Autowired MemberRepository memberRepository;
    @Autowired AgentOfferingRepository offeringRepository;
    @Autowired AgentRequestRepository requestRepository;
    @Autowired EngagementRepository engagementRepository;
    @Autowired RecommendationRepository recommendationRepository;
    @Autowired CreditTransactionRepository creditTransactionRepository;

    @Test
    @DisplayName("참여 생성은 잔액·슬롯·요청 상태·참여·원장을 함께 변경한다")
    void createChangesAllRelatedState() {
        long transactionCount = creditTransactionRepository.count();

        EngagementResponse result = engagementService.create(2L, 201L, 101L);

        assertThat(result.status()).isEqualTo(EngagementStatus.ACTIVE);
        assertThat(result.compatibilityScore()).isEqualTo(99);
        assertThat(memberRepository.findById(2L).orElseThrow().getCreditBalance()).isEqualTo(6);
        assertThat(offeringRepository.findById(101L).orElseThrow().getAvailableSlots()).isEqualTo(2);
        assertThat(requestRepository.findById(201L).orElseThrow().getStatus()).isEqualTo(RequestStatus.MATCHED);
        assertThat(creditTransactionRepository.count()).isEqualTo(transactionCount + 1);
    }

    @Test
    @DisplayName("크레딧 부족이면 관련 상태가 전혀 바뀌지 않는다")
    void insufficientCreditsRollsBack() {
        int slots = offeringRepository.findById(101L).orElseThrow().getAvailableSlots();
        long engagementCount = engagementRepository.count();
        long transactionCount = creditTransactionRepository.count();

        assertThatThrownBy(() -> engagementService.create(3L, 202L, 101L))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_CREDITS));

        assertThat(memberRepository.findById(3L).orElseThrow().getCreditBalance()).isEqualTo(1);
        assertThat(offeringRepository.findById(101L).orElseThrow().getAvailableSlots()).isEqualTo(slots);
        assertThat(requestRepository.findById(202L).orElseThrow().getStatus()).isEqualTo(RequestStatus.OPEN);
        assertThat(engagementRepository.count()).isEqualTo(engagementCount);
        assertThat(creditTransactionRepository.count()).isEqualTo(transactionCount);
    }

    @Test
    @DisplayName("자신의 Offering에는 참여할 수 없다")
    void selfEngagementIsRejected() {
        AgentRequestResponse request = requestService.create(1L, buildHarnessRequest());

        assertThatThrownBy(() -> engagementService.create(1L, request.id(), 101L))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.SELF_ENGAGEMENT_NOT_ALLOWED));
    }

    @Test
    @DisplayName("요청과 유형·도메인·환경이 맞지 않으면 참여할 수 없다")
    void incompatibleOfferingIsRejected() {
        assertThatThrownBy(() -> engagementService.create(2L, 201L, 102L))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_COMPATIBLE));
    }

    @Test
    @DisplayName("활성 참여가 있는 같은 요청은 중복 참여할 수 없다")
    void duplicateActiveEngagementIsRejected() {
        engagementService.create(2L, 201L, 101L);

        assertThatThrownBy(() -> engagementService.create(2L, 201L, 101L))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.REQUEST_NOT_OPEN));
    }

    @Test
    @DisplayName("취소는 크레딧과 슬롯을 복구하고 요청을 다시 연다")
    void cancelRestoresState() {
        EngagementResponse created = engagementService.create(2L, 201L, 101L);

        EngagementResponse canceled = engagementService.cancel(2L, created.id());

        assertThat(canceled.status()).isEqualTo(EngagementStatus.CANCELED);
        assertThat(memberRepository.findById(2L).orElseThrow().getCreditBalance()).isEqualTo(10);
        assertThat(offeringRepository.findById(101L).orElseThrow().getAvailableSlots()).isEqualTo(3);
        assertThat(requestRepository.findById(201L).orElseThrow().getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    @DisplayName("요청자가 아닌 회원은 참여를 취소할 수 없다")
    void unauthorizedCancelIsRejected() {
        EngagementResponse created = engagementService.create(2L, 201L, 101L);

        assertThatThrownBy(() -> engagementService.cancel(3L, created.id()))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("제공자 완료 처리는 보상·완료 건수·요청 상태를 함께 변경한다")
    void completeRewardsProvider() {
        int providerCredits = memberRepository.findById(1L).orElseThrow().getCreditBalance();
        int completedCount = offeringRepository.findById(101L).orElseThrow().getCompletedCount();
        EngagementResponse created = engagementService.create(2L, 201L, 101L);

        EngagementResponse completed = engagementService.complete(1L, created.id());

        assertThat(completed.status()).isEqualTo(EngagementStatus.COMPLETED);
        assertThat(memberRepository.findById(1L).orElseThrow().getCreditBalance()).isEqualTo(providerCredits + 4);
        assertThat(offeringRepository.findById(101L).orElseThrow().getCompletedCount()).isEqualTo(completedCount + 1);
        assertThat(requestRepository.findById(201L).orElseThrow().getStatus()).isEqualTo(RequestStatus.CLOSED);
    }

    @Test
    @DisplayName("완료 전 참여에는 추천을 등록할 수 없다")
    void activeEngagementCannotBeRecommended() {
        EngagementResponse created = engagementService.create(2L, 201L, 101L);

        assertThatThrownBy(() -> recommendationService.create(2L, created.id(), "완료 전 추천입니다."))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_ENGAGEMENT_STATUS));
    }

    @Test
    @DisplayName("완료한 참여는 한 번 추천할 수 있고 추천 수가 증가한다")
    void completedEngagementCanBeRecommendedOnce() {
        int recommendationCount = offeringRepository.findById(101L).orElseThrow().getRecommendationCount();
        EngagementResponse created = engagementService.create(2L, 201L, 101L);
        engagementService.complete(1L, created.id());

        recommendationService.create(2L, created.id(), "검증 기준과 결과가 명확했습니다.");

        assertThat(recommendationRepository.existsByEngagementId(created.id())).isTrue();
        assertThat(offeringRepository.findById(101L).orElseThrow().getRecommendationCount())
            .isEqualTo(recommendationCount + 1);
        assertThatThrownBy(() -> recommendationService.create(2L, created.id(), "중복 추천입니다."))
            .isInstanceOfSatisfying(ApiException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.RECOMMENDATION_ALREADY_EXISTS));
    }

    private AgentRequestRequest buildHarnessRequest() {
        return new AgentRequestRequest(
            "내 API 검토 Harness 요청",
            "Spring Boot API의 트랜잭션과 오류를 검토합니다.",
            RequestType.BUILD_AGENT,
            ArtifactType.HARNESS,
            AgentDomain.DEVELOPMENT,
            ExecutionEnvironment.LOCAL,
            16,
            5,
            8,
            "외부 코드 업로드 없이 사용합니다.",
            "테스트 결과와 복구 체크리스트를 제공합니다."
        );
    }
}

