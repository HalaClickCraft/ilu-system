package com.ilu.system.operator.repository;

import com.ilu.system.operator.entity.WorkstationFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkstationFormationRepository extends JpaRepository<WorkstationFormation, Long> {

    List<WorkstationFormation> findByOperator_Id(Long operatorId);

    List<WorkstationFormation> findByOperator_IdAndStatus(Long operatorId, String status);

    List<WorkstationFormation> findByStatus(String status);

    java.util.Optional<WorkstationFormation> findByOperator_IdAndWorkstation_Id(Long operatorId, Long workstationId);

    boolean existsByOperator_IdAndWorkstation_IdAndStatus(Long operatorId, Long workstationId, String status);

    long countByStatus(String status);

    void deleteByOperator_Id(Long operatorId);
}
