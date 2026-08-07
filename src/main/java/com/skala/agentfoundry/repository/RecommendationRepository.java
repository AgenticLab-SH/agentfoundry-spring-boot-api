package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    boolean existsByEngagementId(Long engagementId);

    @EntityGraph(attributePaths = {"engagement", "offering", "member"})
    Page<Recommendation> findByOfferingId(Long offeringId, Pageable pageable);
}

