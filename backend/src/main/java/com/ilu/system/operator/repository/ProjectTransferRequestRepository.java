package com.ilu.system.operator.repository;

import com.ilu.system.operator.entity.ProjectTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectTransferRequestRepository extends JpaRepository<ProjectTransferRequest, Long> {
    List<ProjectTransferRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<ProjectTransferRequest> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
}
