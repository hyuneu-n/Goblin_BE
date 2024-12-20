package goblin.app.Group.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import goblin.app.Group.model.entity.AvailableTime;

@Repository
public interface AvailableTimeRepository extends JpaRepository<AvailableTime, Long> {
  List<AvailableTime> findByCalendarId(Long calendarId);

  List<AvailableTime> findAllByCalendarId(Long calendarId);

  @Query("SELECT COUNT(DISTINCT a.user) FROM AvailableTime a WHERE a.calendarId = :calendarId")
  Long countDistinctUsersByCalendarId(Long calendarId);
}
