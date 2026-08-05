package com.ilu.system.operator.repository;

import com.ilu.system.operator.entity.OperatorOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorOnboardingRepository extends JpaRepository<OperatorOnboarding, Long> {

    List<OperatorOnboarding> findByOperatorId(Long operatorId);

    Optional<OperatorOnboarding> findByOperatorIdAndModuleId(Long operatorId, Long moduleId);

    @Query("SELECT COUNT(o) FROM OperatorOnboarding o WHERE o.operatorId = :operatorId AND o.completed = true")
    long countCompletedByOperatorId(@Param("operatorId") Long operatorId);

    @Query("SELECT COUNT(o) FROM OperatorOnboarding o WHERE o.operatorId = :operatorId AND o.completed = true AND o.moduleId IN :moduleIds")
    long countCompletedByOperatorIdAndModuleIds(@Param("operatorId") Long operatorId, @Param("moduleIds") List<Long> moduleIds);
}