package goblin.app.Category.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import goblin.app.Category.model.entity.CategoryVisibility;

@Getter
@NoArgsConstructor
public class categoryVisibilityResponseDto {
  private Long categoryId;
  private Long groupId;
  private Boolean visibility;

  @Builder
  public categoryVisibilityResponseDto(CategoryVisibility visibility) {
    this.categoryId = visibility.getCategory().getId();
    this.groupId = visibility.getGroup().getGroupId();
    this.visibility = visibility.getVisibility();
  }
}
