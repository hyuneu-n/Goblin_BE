package goblin.app.FixedSchedule.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Category.model.entity.Category;
import goblin.app.FixedSchedule.model.entity.FixedSchedule;
import goblin.app.User.model.entity.User;

public interface FixedScheduleRepository extends JpaRepository<FixedSchedule, Long> {

  @Query("SELECT fs FROM FixedSchedule fs JOIN FETCH fs.dayOfWeek WHERE fs.user = :user")
  List<FixedSchedule> findByUser(@Param("user") User user);

  // 카테고리로 고정 일정 조회
  List<FixedSchedule> findByCategory(Category category);

  // 고정 일정과 겹치는지 확인하는 메서드
  boolean existsByUserIdAndDayOfWeekContainingAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
      Long userId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
}