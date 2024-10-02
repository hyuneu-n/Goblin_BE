package goblin.app.Group.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.GroupConfirmedCalendar;

public interface GroupConfirmedCalendarRepository
    extends JpaRepository<GroupConfirmedCalendar, Long> {
  List<GroupConfirmedCalendar> findAllByGroupId(Long groupId);

  List<GroupConfirmedCalendar> findByGroupIdAndCalendarId(Long groupId, Long calendarId);
}