package goblin.app.Category.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryVisibility;
import goblin.app.Group.model.entity.Group;
import goblin.app.User.model.entity.User;

@Getter
@NoArgsConstructor
public class categoryViEditRequestDto {
  Long categoryId;
  Long groupId;

  Boolean visibility;

  @Builder
  public categoryViEditRequestDto(Long categoryId, Long groupId, Boolean visibility) {
    this.categoryId = categoryId;
    this.groupId = groupId;
    this.visibility = visibility;
  }

  public CategoryVisibility toEntity(Category category, User user, Group group) {
    return CategoryVisibility.builder()
        .category(category)
        .user(user)
        .group(group)
        .visibility(visibility)
        .build();
  }
}
