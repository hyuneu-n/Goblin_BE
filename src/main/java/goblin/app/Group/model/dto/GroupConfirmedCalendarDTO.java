package goblin.app.Group.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import goblin.app.Group.model.entity.GroupConfirmedCalendar;

@Getter
@Setter
public class GroupConfirmedCalendarDTO {

  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private LocalDate confirmedDate;

  private String title;
  private String place;
  private String note;

  // 인자를 받는 생성자 추가
  public GroupConfirmedCalendarDTO(
      LocalDate confirmedDate,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String title,
      String place,
      String note) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.confirmedDate = confirmedDate;
    this.title = title;
    this.place = place;
    this.note = note;
  }

  // 기본 생성자
  public GroupConfirmedCalendarDTO(GroupConfirmedCalendar calendar) {
    this.startDateTime = calendar.getConfirmedStartTime();
    this.endDateTime = calendar.getConfirmedEndTime();
    this.title = calendar.getTitle();
    this.place = calendar.getPlace();
    this.note = calendar.getNote();

    // startDateTime이 null이 아닌 경우에만 confirmedDate를 설정
    if (this.startDateTime != null) {
      this.confirmedDate = this.startDateTime.toLocalDate();
    }
  }
}
