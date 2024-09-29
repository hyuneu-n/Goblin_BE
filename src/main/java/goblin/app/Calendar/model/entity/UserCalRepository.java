package goblin.app.Calendar.model.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCalRepository extends JpaRepository<UserCalendar,Long> {
    @Query("SELECT u FROM UserCalendar u WHERE FUNCTION('YEAR', u.date) = :year " +
            "AND FUNCTION('MONTH', u.date) = :month")
    List<UserCalendar> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT u FROM UserCalendar u WHERE FUNCTION('YEAR', u.date) = :year " +
            "AND FUNCTION('MONTH', u.date) = :month " +
            "AND FUNCTION('DAY', u.date) = :day")
    List<UserCalendar> findByDay(@Param("year") int year, @Param("month") int month, @Param("day") int day);

}
