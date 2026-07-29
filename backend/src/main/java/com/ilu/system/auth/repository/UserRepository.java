package com.ilu.system.auth.repository;
import com.ilu.system.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByNationalId(String nationalId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByNationalId(String nationalId);
}
