package com.skala.agentfoundry.common;

import com.skala.agentfoundry.exception.ApiException;
import com.skala.agentfoundry.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;

public final class SessionMember {

    public static final String MEMBER_ID = "LOGIN_MEMBER_ID";

    private SessionMember() {
    }

    public static Long require(HttpSession session) {
        Object value = session.getAttribute(MEMBER_ID);
        if (value instanceof Long memberId) {
            return memberId;
        }
        throw new ApiException(ErrorCode.NOT_AUTHENTICATED);
    }
}

