package goblin.app.Group.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
  Optional<Group> findByGroupName(String groupName);
}
