package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Group.model.entity.GroupConfirmedCalendar;

public interface GroupConfirmedCalendarRepository
    extends JpaRepository<GroupConfirmedCalendar, Long> {

  List<GroupConfirmedCalendar> findAllByGroupId(Long groupId);

  Optional<GroupConfirmedCalendar> findByGroupIdAndCalendarId(Long groupId, Long calendarId);

  Optional<GroupConfirmedCalendar> findByCalendarId(Long calendarId);

  @Query(
      "SELECT c FROM GroupConfirmedCalendar c WHERE c.groupId = :groupId AND YEAR(c.confirmedStartTime) = :year AND MONTH(c.confirmedStartTime) = :month")
  List<GroupConfirmedCalendar> findAllByGroupIdAndMonth(
      @Param("groupId") Long groupId, @Param("year") int year, @Param("month") int month);

  @Query(
      "SELECT c FROM GroupConfirmedCalendar c WHERE c.groupId = :groupId AND YEAR(c.confirmedStartTime) = :year AND MONTH(c.confirmedStartTime) = :month AND DAY(c.confirmedStartTime) = :day")
  List<GroupConfirmedCalendar> findAllByGroupIdAndDay(
      @Param("groupId") Long groupId,
      @Param("year") int year,
      @Param("month") int month,
      @Param("day") int day);
}
