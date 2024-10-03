package goblin.app.Category.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import goblin.app.Category.model.entity.Category;

@Getter
@NoArgsConstructor
public class categoryResponseDto {

  private Long id;
  private String categoryName;
  private String color;

  @Builder
  public categoryResponseDto(Category entity) {
    this.id = entity.getId();
    this.categoryName = entity.getCategoryName();
    this.color = entity.getColor();
  }
}
