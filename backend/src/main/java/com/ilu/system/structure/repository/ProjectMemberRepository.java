package com.ilu.system.structure.repository;
import com.ilu.system.structure.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByEmployeeId(String employeeId);
    boolean existsByProjectIdAndEmployeeId(Long projectId, String employeeId);
}
