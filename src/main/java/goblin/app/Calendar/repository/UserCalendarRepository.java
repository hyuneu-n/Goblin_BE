package goblin.app.Calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import goblin.app.Calendar.model.entity.UserCalendar;

@Repository
public interface UserCalendarRepository extends JpaRepository<UserCalendar, Long> {}
