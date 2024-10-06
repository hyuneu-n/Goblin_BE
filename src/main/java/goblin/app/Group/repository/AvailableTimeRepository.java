package goblin.app.Group.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import goblin.app.Group.model.entity.AvailableTime;

@Repository
public interface AvailableTimeRepository extends JpaRepository<AvailableTime, Long> {
  List<AvailableTime> findByCalendarId(Long calendarId);

  // 사용자와 캘린더 ID로 가능한 시간 삭제
  void deleteByCalendarIdAndUserId(Long calendarId, Long loginId);
}
