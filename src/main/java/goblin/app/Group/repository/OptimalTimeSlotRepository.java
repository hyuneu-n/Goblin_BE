package goblin.app.Group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import goblin.app.Group.model.entity.OptimalTimeSlot;

@Repository
public interface OptimalTimeSlotRepository extends JpaRepository<OptimalTimeSlot, Long> {}
