package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByEmployeeId(String employeeId);
    List<Operator> findByTeamId(Long teamId);
    List<Operator> findByActiveTrue();
    List<Operator> findByActiveFalse();
    long countByActiveTrue();
    boolean existsByEmployeeId(String employeeId);
    @Query("SELECT COUNT(o) FROM Operator o WHERE o.exitDate IS NULL")
    long countActiveOperators();
    @Query("SELECT o FROM Operator o WHERE o.team.id = :teamId AND o.active = true")
    List<Operator> findActiveByTeamId(Long teamId);
}
