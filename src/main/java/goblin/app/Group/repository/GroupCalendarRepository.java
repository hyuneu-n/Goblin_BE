package goblin.app.Group.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.GroupCalendar;

public interface GroupCalendarRepository extends JpaRepository<GroupCalendar, Long> {
  // 특정 그룹의 모든 일정을 조회하는 메서드
  List<GroupCalendar> findAllByGroupId(Long groupId);
}
