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
  private int colorCode; // 색상 코드 추가

  @Builder
  public categorySaveRequestDto(String categoryName, int colorCode) {
    this.categoryName = categoryName;
    this.colorCode = colorCode;
  }

  public Category toEntity(User user, String color) {
    return Category.builder()
        .categoryName(categoryName)
        .user(user)
        .color(color) // 색상 값을 전달받아 설정
        .build();
  }
}
