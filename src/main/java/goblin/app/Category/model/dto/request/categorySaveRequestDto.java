package goblin.app.Category.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;

@Getter
@NoArgsConstructor
public class categorySaveRequestDto {

  @NotBlank private String categoryName;

  @Builder
  public categorySaveRequestDto(String categoryName) {
    this.categoryName = categoryName;
  }

  public Category toEntity(User user) {
    return Category.builder().categoryName(categoryName).user(user).build();
  }
}
