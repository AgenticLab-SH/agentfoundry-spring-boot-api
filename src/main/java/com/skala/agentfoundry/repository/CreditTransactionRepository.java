package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    Page<CreditTransaction> findByMemberId(Long memberId, Pageable pageable);

    long countByMemberId(Long memberId);
}
