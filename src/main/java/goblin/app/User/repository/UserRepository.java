package goblin.app.User.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.User.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByLoginId(String loginId);

  List<User> findByLoginIdContaining(String loginId); // 부분 일치 검색
}
