package goblin.app.Calendar.model.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;

public interface UserCalRepository extends JpaRepository<UserCalendar, Long> {
  @Query(
      "SELECT u FROM UserCalendar u WHERE FUNCTION('YEAR', u.startTime) = :year "
          + "AND FUNCTION('MONTH', u.startTime) = :month "
          + "AND u.user = :user")
  List<UserCalendar> findByYearAndMonth(
      @Param("year") int year, @Param("month") int month, @Param("user") User user);

  @Query(
      "SELECT u FROM UserCalendar u WHERE FUNCTION('YEAR', u.startTime) = :year "
          + "AND FUNCTION('MONTH', u.startTime) = :month "
          + "AND FUNCTION('DAY', u.startTime) = :day "
          + "AND u.user = :user")
  List<UserCalendar> findByDay(
      @Param("year") int year,
      @Param("month") int month,
      @Param("day") int day,
      @Param("user") User user);

  @Query("SELECT u FROM UserCalendar u WHERE u.category = :category AND u.user = :user")
  List<UserCalendar> findByCategoryAndUser(
      @Param("category") Category category, @Param("user") User user);

  List<UserCalendar> findByTitleContainingAndUser(String title, User user);

  @Query("SELECT u FROM UserCalendar u WHERE u.user = :user AND u.isFixed = true")
  List<UserCalendar> findFixedSchedulesByUser(@Param("user") User user);
}
