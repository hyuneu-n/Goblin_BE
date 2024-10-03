package goblin.app.Category.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class categoryEditRequestDto {

  private Long categoryId; // 수정할 카테고리의 ID 필드 추가
  @NotBlank private String categoryName;
  private int colorCode;

  @Builder
  public categoryEditRequestDto(Long categoryId, String categoryName, int colorCode) {
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.colorCode = colorCode;
  }

  // 색상 코드 번호에 따라 미리 지정된 색상 값을 반환하는 메서드
  public String resolveColorCode() {
    switch (colorCode) {
      case 1:
        return "F3DAD8";
      case 2:
        return "F1DAED";
      case 3:
        return "F2EDD9";
      case 4:
        return "E6E8E3";
      case 5:
        return "B1B0B5";
      default:
        throw new IllegalArgumentException("유효하지 않은 색상 코드입니다: " + colorCode);
    }
  }
}
