package goblin.app.Calendar.model.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCalRepository extends JpaRepository<UserCalendar,Long> {
}
