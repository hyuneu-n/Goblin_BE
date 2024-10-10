package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;

public interface GroupCalendarRepository extends JpaRepository<GroupCalendar, Long> {
  // 특정 그룹의 모든 일정을 조회하는 메서드
  List<GroupCalendar> findAllByGroup(Group group);

  // 그룹 ID와 일정 ID로 특정 일정을 조회하는 메서드
  @Query("SELECT gc FROM GroupCalendar gc WHERE gc.group.id = :groupId AND gc.id = :calendarId")
  Optional<GroupCalendar> findByGroupIdAndCalendarId(
      @Param("groupId") Long groupId, @Param("calendarId") Long calendarId);
}
