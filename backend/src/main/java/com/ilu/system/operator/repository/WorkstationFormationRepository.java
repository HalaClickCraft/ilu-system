package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.WorkstationFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WorkstationFormationRepository extends JpaRepository<WorkstationFormation, Long> {
    List<WorkstationFormation> findByOperatorId(Long operatorId);
    List<WorkstationFormation> findByWorkstationId(Long workstationId);
    List<WorkstationFormation> findByStatus(String status);
    long countByStatus(String status);
    @Query("SELECT f FROM WorkstationFormation f WHERE f.operator.id = :operatorId AND f.workstation.id = :workstationId AND f.status = 'IN_PROGRESS'")
    List<WorkstationFormation> findActiveByOperatorAndWorkstation(Long operatorId, Long workstationId);
}
