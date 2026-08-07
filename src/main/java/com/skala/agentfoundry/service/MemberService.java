package com.skala.agentfoundry.service;

import com.skala.agentfoundry.domain.CreditTransaction;
import com.skala.agentfoundry.domain.Member;
import com.skala.agentfoundry.dto.LoginRequest;
import com.skala.agentfoundry.dto.MemberCreateRequest;
import com.skala.agentfoundry.dto.MemberResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.CreditTransactionRepository;
import com.skala.agentfoundry.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int INITIAL_CREDITS = 5;

    private final MemberRepository memberRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final Clock clock;

    @Transactional
    public MemberResponse register(MemberCreateRequest request) {
        if (memberRepository.existsByMemberId(request.memberId())) {
            throw new ApiException(ErrorCode.DATA_DUPLICATED, "이미 사용 중인 회원 ID입니다.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Member member = Member.create(
            request.memberId(),
            request.password(),
            request.displayName(),
            INITIAL_CREDITS,
            now
        );
        Member saved = memberRepository.save(member);
        creditTransactionRepository.save(CreditTransaction.initial(saved, INITIAL_CREDITS, now));
        return MemberResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse authenticate(LoginRequest request) {
        Member member = memberRepository.findByMemberId(request.memberId())
            .filter(candidate -> candidate.passwordMatches(request.password()))
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_AUTHENTICATED, "회원 ID 또는 비밀번호가 올바르지 않습니다."));
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        return MemberResponse.from(getMemberEntity(memberId));
    }

    @Transactional(readOnly = true)
    public Member getMemberEntity(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}

