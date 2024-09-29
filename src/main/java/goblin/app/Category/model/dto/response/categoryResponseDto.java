package goblin.app.Category.model.dto.response;


import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class categoryResponseDto {

    private Long id;
    private String categoryName;

    @Builder
    public categoryResponseDto(Category entity) {
        this.id = entity.getId();
        this.categoryName = entity.getCategoryName();
    }
}
