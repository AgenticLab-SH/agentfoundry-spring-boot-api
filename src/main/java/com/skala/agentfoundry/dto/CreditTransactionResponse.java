package com.skala.agentfoundry.dto;

import com.skala.agentfoundry.domain.CreditTransaction;
import com.skala.agentfoundry.domain.CreditTransactionType;
import java.time.LocalDateTime;

public record CreditTransactionResponse(
    Long id,
    Long engagementId,
    CreditTransactionType type,
    Integer amount,
    Integer balanceAfter,
    String description,
    LocalDateTime createdAt
) {

    public static CreditTransactionResponse from(CreditTransaction transaction) {
        return new CreditTransactionResponse(
            transaction.getId(),
            transaction.getEngagement() == null ? null : transaction.getEngagement().getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getDescription(),
            transaction.getCreatedAt()
        );
    }
}
