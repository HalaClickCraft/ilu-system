package com.ilu.system.structure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjet_IdProjetAndUtilisateur_Id(Long projectId, Long userId);
    boolean existsByProjet_IdProjetAndUtilisateur_IdAndIdNot(Long projectId, Long userId, Long memberId);
    List<ProjectMember> findByProjet_IdProjet(Long projectId);
    Optional<ProjectMember> findByIdAndProjet_IdProjet(Long memberId, Long projectId);
}