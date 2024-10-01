package goblin.app.Category.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class categoryEditRequestDto {
  private Long categoryId;

  @NotBlank private String categoryName;
}
