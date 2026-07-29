package com.ilu.system.structure.repository;
import com.ilu.system.structure.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByProjectId(Long projectId);
    boolean existsByNameAndProjectId(String name, Long projectId);
}
