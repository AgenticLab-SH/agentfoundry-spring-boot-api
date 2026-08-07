package com.skala.agentfoundry.service;

import com.skala.agentfoundry.common.PageResponse;
import com.skala.agentfoundry.dto.CreditTransactionResponse;
import com.skala.agentfoundry.dto.MemberResponse;
import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import com.skala.agentfoundry.repository.CreditTransactionRepository;
import com.skala.agentfoundry.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final MemberRepository memberRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Transactional(readOnly = true)
    public MemberResponse getBalance(Long memberId) {
        return memberRepository.findById(memberId)
            .map(MemberResponse::from)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public PageResponse<CreditTransactionResponse> getTransactions(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND, "회원을 찾을 수 없습니다.");
        }
        Page<CreditTransactionResponse> result = creditTransactionRepository.findByMemberId(memberId, pageable)
            .map(CreditTransactionResponse::from);
        return PageResponse.from(result);
    }
}

