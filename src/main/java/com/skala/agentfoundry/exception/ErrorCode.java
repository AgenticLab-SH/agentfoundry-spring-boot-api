package com.skala.agentfoundry.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다."),
    DATA_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 데이터입니다."),
    OFFERING_NOT_OPEN(HttpStatus.CONFLICT, "현재 참여할 수 없는 Offering입니다."),
    OFFERING_FULL(HttpStatus.CONFLICT, "제공 가능한 슬롯이 없습니다."),
    REQUEST_NOT_OPEN(HttpStatus.CONFLICT, "현재 참여를 연결할 수 없는 요청입니다."),
    RESOURCE_NOT_COMPATIBLE(HttpStatus.CONFLICT, "요청 조건과 실행 리소스가 호환되지 않습니다."),
    BUDGET_EXCEEDED(HttpStatus.CONFLICT, "요청의 최대 크레딧을 초과합니다."),
    CAPACITY_BELOW_ACTIVE(HttpStatus.CONFLICT, "제공 인원을 현재 활성 참여 수보다 줄일 수 없습니다."),
    SELF_ENGAGEMENT_NOT_ALLOWED(HttpStatus.CONFLICT, "자신의 Offering에는 참여할 수 없습니다."),
    INSUFFICIENT_CREDITS(HttpStatus.CONFLICT, "참여에 필요한 크레딧이 부족합니다."),
    ENGAGEMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이 요청에는 이미 활성 참여가 있습니다."),
    INVALID_ENGAGEMENT_STATUS(HttpStatus.CONFLICT, "현재 참여 상태에서는 처리할 수 없습니다."),
    ACTIVE_ENGAGEMENT_EXISTS(HttpStatus.CONFLICT, "활성 참여가 있어 보관 처리할 수 없습니다."),
    RECOMMENDATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 추천을 등록한 참여입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
