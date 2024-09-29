package goblin.app.Category.model.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class categoryEditRequestDto {
    private Long categoryId;

    @NotBlank
    private String categoryName;

}
