package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import goblin.app.Group.model.entity.GroupCalendar;

public interface GroupCalendarRepository extends JpaRepository<GroupCalendar, Long> {
  // 특정 그룹의 모든 일정을 조회하는 메서드
  List<GroupCalendar> findAllByGroupId(Long groupId);

  // 삭제되지 않은 일정만 조회하는 쿼리
  @Query("SELECT gc FROM GroupCalendar gc WHERE gc.groupId = :groupId AND gc.deleted = false")
  List<GroupCalendar> findAllByGroupIdAndNotDeleted(Long groupId);

  // 확정된 일정만 조회하는 쿼리 메서드
  Optional<GroupCalendar> findByIdAndGroupIdAndConfirmed(
      Long calendarId, Long groupId, boolean confirmed);
}
