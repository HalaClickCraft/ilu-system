package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByNameContaining(String name);
    boolean existsByName(String name);
    Optional<Team> findByTeamLeaderEmployeeId(String teamLeaderEmployeeId);
    List<Team> findByProjectId(Long projectId);
}
