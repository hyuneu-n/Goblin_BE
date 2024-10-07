package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;
import org.springframework.data.jpa.repository.Query;

public interface GroupCalendarRepository extends JpaRepository<GroupCalendar, Long> {
  // 특정 그룹의 모든 일정을 조회하는 메서드
  List<GroupCalendar> findAllByGroup(Group group);

  // 확정된 일정만 조회하는 쿼리 메서드
  Optional<GroupCalendar> findByIdAndGroupAndConfirmed(
      Long calendarId, Group group, boolean confirmed);

  @Query("SELECT COUNT(cu.user) FROM GroupCalendar cu WHERE cu.calendar.id = :calendarId")
  Long countUsersByCalendarId(Long calendarId);
}
