package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;

public interface GroupCalendarRepository extends JpaRepository<GroupCalendar, Long> {

  // 특정 그룹의 모든 일정을 조회하는 메서드
  List<GroupCalendar> findAllByGroup(Group group);

  // 그룹 ID와 일정 ID로 특정 일정을 조회하는 메서드
  @Query("SELECT gc FROM GroupCalendar gc WHERE gc.group.id = :groupId AND gc.id = :calendarId")
  Optional<GroupCalendar> findByGroupIdAndCalendarId(
      @Param("groupId") Long groupId, @Param("calendarId") Long calendarId);

  // 특정 그룹에 속한 active(삭제되지 않은) 일정 조회
  @Query("SELECT c FROM GroupCalendar c WHERE c.group.id = :groupId AND c.deleted = false")
  List<GroupCalendar> findAllByGroupId(@Param("groupId") Long groupId);

  // 특정 그룹에 속한 active(삭제되지 않은) 일정만 조회
  @Query("SELECT g FROM GroupCalendar g WHERE g.group.id = :groupId AND g.deleted = false")
  List<GroupCalendar> findAllActiveByGroupId(@Param("groupId") Long groupId);

  // soft delete 수행 (삭제 대신 deleted 플래그를 true로 설정)
  @Modifying
  @Transactional // 트랜잭션 처리 필요
  @Query("UPDATE GroupCalendar g SET g.deleted = true WHERE g.id = :id")
  void softDeleteById(@Param("id") Long id);
}
