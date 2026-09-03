package com.ilu.system.absence.repository;

import com.ilu.system.absence.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.ilu.system.absence.entity.Absence.AbsenceStatus;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    List<Absence> findByOperator_Id(Long operatorId);

    List<Absence> findByStatus(AbsenceStatus status);

    Optional<Absence> findByOperator_IdAndStatus(Long operatorId, AbsenceStatus status);

    boolean existsByOperator_IdAndStatus(Long operatorId, AbsenceStatus status);

    void deleteByOperator_Id(Long operatorId);
}