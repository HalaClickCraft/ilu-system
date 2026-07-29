package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByNameContaining(String name);
    boolean existsByName(String name);
}
