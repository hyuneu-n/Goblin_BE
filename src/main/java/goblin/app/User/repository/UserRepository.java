package goblin.app.User.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.User.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByLoginId(String loginId);
}
