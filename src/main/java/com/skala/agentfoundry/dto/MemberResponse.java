package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.Member;

public record MemberResponse(
    Long id,
    String memberId,
    String displayName,
    Integer creditBalance
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getMemberId(),
            member.getDisplayName(),
            member.getCreditBalance()
        );
    }
}

