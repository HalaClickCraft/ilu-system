package com.ilu.system.operator.repository;

import com.ilu.system.operator.entity.TeamUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamUpdateRequestRepository extends JpaRepository<TeamUpdateRequest, Long> {
    List<TeamUpdateRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<TeamUpdateRequest> findByTeamIdOrderByCreatedAtDesc(Long teamId);
}
