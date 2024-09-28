package goblin.app.Calendar.model.dto.response;

import goblin.app.Calendar.model.entity.UserCalendar;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class uCalResponseDto {
    Long id;
    String title;
    String categoryName;

    String note;

    LocalDateTime startTime;
    LocalDateTime endTime;

    @Builder
    public uCalResponseDto(UserCalendar entity) {
        this.categoryName = entity.getCategory().getCategoryName();
        this.id = entity.getId();
        this.note = entity.getNote();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
    }

}
