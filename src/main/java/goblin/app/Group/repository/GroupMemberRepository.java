package goblin.app.Group.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import goblin.app.Group.model.entity.GroupMember;
import goblin.app.User.model.entity.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
  Optional<GroupMember> findByGroupIdAndUser(Long groupId, User user); // User 객체로 검색
}
