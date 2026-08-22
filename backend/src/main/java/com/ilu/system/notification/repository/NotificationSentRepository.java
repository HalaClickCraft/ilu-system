package com.ilu.system.notification.repository;

import com.ilu.system.notification.entity.NotificationSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSentRepository extends JpaRepository<NotificationSent, Long> {

    @Query("""
            select (count(notificationSent) > 0)
            from NotificationSent notificationSent
            where notificationSent.planningId = :planningId
              and notificationSent.daysBefore = :daysBefore
            """)
    boolean existsByPlanningIdAndDaysBefore(
            @Param("planningId") Long planningId,
            @Param("daysBefore") Integer daysBefore);
}
