package com.skala.agentfoundry.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String code,
    String message,
    String path,
    List<FieldErrorDetail> fieldErrors
) {

    public record FieldErrorDetail(String field, String message) {
    }
}

