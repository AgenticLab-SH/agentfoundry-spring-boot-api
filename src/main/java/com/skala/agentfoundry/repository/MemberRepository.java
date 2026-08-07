package com.skala.agentfoundry.repository;

import com.skala.agentfoundry.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);
}

