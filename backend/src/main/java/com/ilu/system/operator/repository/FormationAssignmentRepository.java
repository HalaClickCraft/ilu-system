package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.FormationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface FormationAssignmentRepository extends JpaRepository<FormationAssignment, Long> {
    List<FormationAssignment> findByOperatorId(Long operatorId);
    List<FormationAssignment> findByIsPrimaryAssignmentTrueAndOperatorId(Long operatorId);
    List<FormationAssignment> findByWorkstationId(Long workstationId);
    void deleteByOperatorId(Long operatorId);
}
