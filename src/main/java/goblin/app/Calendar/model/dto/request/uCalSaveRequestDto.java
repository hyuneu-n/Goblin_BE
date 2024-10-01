package goblin.app.Calendar.model.dto.request;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;

@Getter
@NoArgsConstructor
public class uCalSaveRequestDto {

  Long categoryId;
  @NotBlank String title;
  String note;
  LocalDateTime startTime;

  LocalDateTime endTime;

  @Builder
  public uCalSaveRequestDto(
      Long categoryId, String title, String note, LocalDateTime startTime, LocalDateTime endTime) {
    this.categoryId = categoryId;
    this.title = title;
    this.note = note;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public UserCalendar toEntity(User user, Category category) {
    return UserCalendar.builder()
        .title(title)
        .user(user)
        .category(category)
        .note(note)
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }
}
