package com.ilu.system.operator.repository;

import com.ilu.system.operator.entity.OnboardingModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingModuleRepository extends JpaRepository<OnboardingModule, Long> {

    List<OnboardingModule> findAllByOrderByDepartmentAscDisplayOrderAsc();

    List<OnboardingModule> findByDepartmentOrderByDisplayOrderAsc(String department);

  @Query("SELECT DISTINCT m.department FROM OnboardingModule m")
List<String> findDistinctDepartments();
}