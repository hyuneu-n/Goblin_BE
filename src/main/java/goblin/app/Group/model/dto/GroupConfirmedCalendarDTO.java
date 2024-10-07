package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupConfirmedCalendarDTO {

  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String title;
  private String place;
  private String note;

  // 인자를 받는 생성자 추가
  public GroupConfirmedCalendarDTO(
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String title,
      String place,
      String note) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.title = title;
    this.place = place;
    this.note = note;
  }

  // 기본 생성자
  public GroupConfirmedCalendarDTO() {}
}
