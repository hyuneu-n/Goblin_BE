package goblin.app.Category.model.dto.request;


import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class categorySaveRequestDto {

    @NotBlank
    String categoryName;

    @Builder
    public Category toEntity(User user) {
        return Category.builder()
                .categoryName(categoryName)
                .user(user)
                .build();
    }
}
