package goblin.app.FixedSchedule.model.dto;

import java.time.DayOfWeek;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateFixedScheduleRequestDTO {
  private List<DayOfWeek> dayOfWeek; // 수정할 요일 리스트
  private String amPmStart; // AM/PM 값
  private int startHour;
  private int startMinute;
  private String amPmEnd; // AM/PM 값
  private int endHour;
  private int endMinute;
}
