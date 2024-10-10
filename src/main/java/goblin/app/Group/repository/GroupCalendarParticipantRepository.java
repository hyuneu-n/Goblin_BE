package goblin.app.Group.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Group.model.entity.GroupCalendarParticipant;
import goblin.app.User.model.entity.User;

public interface GroupCalendarParticipantRepository
    extends JpaRepository<GroupCalendarParticipant, Long> {
  @Transactional
  void deleteAllByCalendarId(Long calendarId);

  Optional<GroupCalendarParticipant> findByCalendarIdAndUser(Long calendarId, User user);

  List<GroupCalendarParticipant> findAllByCalendarId(Long calendarId);

  List<GroupCalendarParticipant> findAllByUserId(Long userId);

  List<GroupCalendarParticipant> findByCalendarId(Long id);

  @Query(
      "SELECT COUNT(DISTINCT p.user) FROM GroupCalendarParticipant p WHERE p.calendarId = :calendarId")
  Long countUsersByCalendarId(@Param("calendarId") Long calendarId);


}
