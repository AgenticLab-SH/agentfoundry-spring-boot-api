package com.skala.agentfoundry.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
        ApiException exception,
        HttpServletRequest request
    ) {
        ErrorCode code = exception.getErrorCode();
        return response(code.status(), code.name(), exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
            .toList();
        return response(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED.name(),
            ErrorCode.VALIDATION_FAILED.message(),
            request,
            fieldErrors
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(
        BindException exception,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = exception.getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
            .toList();
        return response(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED.name(),
            ErrorCode.VALIDATION_FAILED.message(),
            request,
            fieldErrors
        );
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleParameter(
        Exception exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED.name(),
            "요청 파라미터 형식을 확인해 주세요.",
            request,
            List.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED.name(),
            "요청 본문 형식을 확인해 주세요.",
            request,
            List.of()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
        HttpRequestMethodNotSupportedException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            "지원하지 않는 HTTP 메서드입니다.",
            request,
            List.of()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
        NoResourceFoundException exception,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.NOT_FOUND,
            ErrorCode.DATA_NOT_FOUND.name(),
            "요청한 경로를 찾을 수 없습니다.",
            request,
            List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error("Unexpected request failure: method={}, uri={}", request.getMethod(), request.getRequestURI(), exception);
        return response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR.name(),
            ErrorCode.INTERNAL_ERROR.message(),
            request,
            List.of()
        );
    }

    private ResponseEntity<ErrorResponse> response(
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request,
        List<ErrorResponse.FieldErrorDetail> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
            LocalDateTime.now(clock),
            status.value(),
            code,
            message,
            request.getRequestURI(),
            fieldErrors
        ));
    }
}
