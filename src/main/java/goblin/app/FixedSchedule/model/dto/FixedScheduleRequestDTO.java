package goblin.app.FixedSchedule.model.dto;

import java.time.DayOfWeek;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FixedScheduleRequestDTO {
  private Long categoryId;
  private String scheduleName;
  private List<DayOfWeek> dayOfWeek;
  private String amPmStart;
  private int startHour;
  private int startMinute;
  private String amPmEnd;
  private int endHour;
  private int endMinute;
}
