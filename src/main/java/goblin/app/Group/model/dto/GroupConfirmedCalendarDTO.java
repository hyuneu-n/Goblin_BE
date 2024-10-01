package goblin.app.Group.model.dto;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupConfirmedCalendarDTO {

  private LocalTime startTime;
  private LocalTime endTime;
  private String title;
  private String place;
  private String note;

  // 인자를 받는 생성자 추가
  public GroupConfirmedCalendarDTO(
      LocalTime startTime, LocalTime endTime, String title, String place, String note) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.title = title;
    this.place = place;
    this.note = note;
  }

  // 기본 생성자
  public GroupConfirmedCalendarDTO() {}
}
