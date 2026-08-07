package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentOffering;
import com.skala.agentfoundry.domain.ArtifactType;
import com.skala.agentfoundry.domain.ExecutionEnvironment;
import com.skala.agentfoundry.domain.ListingType;
import com.skala.agentfoundry.domain.OfferingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentOfferingRepository extends JpaRepository<AgentOffering, Long> {

    @EntityGraph(attributePaths = "provider")
    @Query("""
        select o from AgentOffering o
        where (:keyword is null or lower(o.title) like lower(concat('%', :keyword, '%'))
            or lower(o.summary) like lower(concat('%', :keyword, '%')))
          and (:listingType is null or o.listingType = :listingType)
          and (:artifactType is null or o.artifactType = :artifactType)
          and (:domain is null or o.domain = :domain)
          and (:environment is null or o.environment = :environment)
          and (:status is null or o.status = :status)
        """)
    Page<AgentOffering> search(
        @Param("keyword") String keyword,
        @Param("listingType") ListingType listingType,
        @Param("artifactType") ArtifactType artifactType,
        @Param("domain") AgentDomain domain,
        @Param("environment") ExecutionEnvironment environment,
        @Param("status") OfferingStatus status,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "provider")
    @Query("select o from AgentOffering o where o.id = :id")
    Optional<AgentOffering> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = "provider")
    List<AgentOffering> findByStatusAndAvailableSlotsGreaterThan(OfferingStatus status, int slots);
}

