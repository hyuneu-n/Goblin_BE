package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.GroupConfirmedCalendar;

public interface GroupConfirmedCalendarRepository
    extends JpaRepository<GroupConfirmedCalendar, Long> {
  List<GroupConfirmedCalendar> findAllByGroupId(Long groupId);

  Optional<GroupConfirmedCalendar> findByGroupIdAndCalendarId(Long groupId, Long calendarId);

  Optional<GroupConfirmedCalendar> findByCalendarId(Long calendarId);
}
