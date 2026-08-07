package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.AgentDomain;
import com.skala.agentfoundry.domain.AgentRequest;
import com.skala.agentfoundry.domain.RequestStatus;
import com.skala.agentfoundry.domain.RequestType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    @EntityGraph(attributePaths = "requester")
    @Query("""
        select r from AgentRequest r
        where (:keyword is null or lower(r.title) like lower(concat('%', :keyword, '%'))
            or lower(r.goal) like lower(concat('%', :keyword, '%')))
          and (:requestType is null or r.requestType = :requestType)
          and (:domain is null or r.domain = :domain)
          and (:status is null or r.status = :status)
        """)
    Page<AgentRequest> search(
        @Param("keyword") String keyword,
        @Param("requestType") RequestType requestType,
        @Param("domain") AgentDomain domain,
        @Param("status") RequestStatus status,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "requester")
    @Query("select r from AgentRequest r where r.id = :id")
    Optional<AgentRequest> findDetailById(@Param("id") Long id);
}

