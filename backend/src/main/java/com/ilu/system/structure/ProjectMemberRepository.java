package com.ilu.system.structure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjet_IdProjetAndUtilisateur_Id(Long projectId, Long userId);
    List<ProjectMember> findByProjet_IdProjet(Long projectId);
}