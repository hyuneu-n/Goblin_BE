package goblin.app.Group.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.GroupCalendarParticipant;

public interface GroupCalendarParticipantRepository
    extends JpaRepository<GroupCalendarParticipant, Long> {}
