package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.Engagement;
import com.skala.agentfoundry.domain.EngagementStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EngagementRepository extends JpaRepository<Engagement, Long> {

    boolean existsByRequestIdAndStatus(Long requestId, EngagementStatus status);

    boolean existsByOfferingIdAndStatus(Long offeringId, EngagementStatus status);

    @EntityGraph(attributePaths = {"request", "offering", "requester", "provider"})
    @Query("select e from Engagement e where e.id = :id")
    Optional<Engagement> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"request", "offering", "requester", "provider"})
    @Query("select e from Engagement e where e.requester.id = :memberId or e.provider.id = :memberId")
    Page<Engagement> findMine(@Param("memberId") Long memberId, Pageable pageable);
}

