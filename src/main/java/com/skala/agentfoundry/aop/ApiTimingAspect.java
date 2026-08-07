package com.skala.agentfoundry.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ApiTimingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpServletResponse response = attributes.getResponse();
                long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
                int status = result instanceof ResponseEntity<?> entity
                    ? entity.getStatusCode().value()
                    : response == null ? 200 : response.getStatus();
                // 본문이나 세션 값은 남기지 않고 요청 경로와 처리 결과만 기록합니다.
                log.info(
                    "api completed method={} uri={} status={} elapsedMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    elapsedMs
                );
            }
            return result;
        } catch (Throwable exception) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
                log.info(
                    "api failed method={} uri={} outcome=EXCEPTION elapsedMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    elapsedMs
                );
            }
            throw exception;
        }
    }
}
