package com.ilu.system.operator.repository;
import com.ilu.system.operator.entity.DailyFormationTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface DailyFormationTrackingRepository extends JpaRepository<DailyFormationTracking, Long> {
    List<DailyFormationTracking> findByFormationId(Long formationId);
    List<DailyFormationTracking> findByFormationIdOrderByTrackingDateDesc(Long formationId);
    List<DailyFormationTracking> findByFormationIdOrderByTrackingDateAsc(Long formationId);
    List<DailyFormationTracking> findByFormationIdAndTrackingDate(Long formationId, LocalDate trackingDate);
}
