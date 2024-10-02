package goblin.app.Category.model.entity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.Group;
import goblin.app.User.model.entity.User;

public interface CategoryVisibilityRepository extends JpaRepository<CategoryVisibility, Long> {

  Optional<CategoryVisibility> findByCategoryAndGroupAndUser(
      Category category, Group group, User user);

  List<CategoryVisibility> findByGroupAndUser(Group group, User user);
}
