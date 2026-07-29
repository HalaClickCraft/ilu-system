package com.ilu.system.structure.repository;
import com.ilu.system.structure.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    List<Workstation> findByZoneId(Long zoneId);
    List<Workstation> findByNameContaining(String name);
    @Query("SELECT w FROM Workstation w WHERE w.zone.project.id = :projectId")
    List<Workstation> findByProjectId(Long projectId);
    boolean existsByName(String name);
}
