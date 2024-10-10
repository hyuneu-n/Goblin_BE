package goblin.app.Group.model.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import goblin.app.Group.model.entity.Group;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.User.model.entity.User;

@Component
public class GroupHelper {

  private final GroupRepository groupRepository;

  @Autowired
  public GroupHelper(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public Group getOrCreatePersonalGroup(User user) {
    // 특정 유저가 생성한 "개인" 그룹을 찾음
    return groupRepository
        .findByGroupNameAndCreatedBy("개인", user)
        .orElseGet(
            () -> {
              // 없으면 "개인" 그룹 생성
              Group personalGroup = new Group();
              personalGroup.setGroupName("개인");
              personalGroup.setCreatedBy(user);
              groupRepository.save(personalGroup);
              return personalGroup;
            });
  }
}
