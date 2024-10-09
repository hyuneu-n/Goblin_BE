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
  private Long groupId;
  private String scheduleName;
  private List<DayOfWeek> dayOfWeek;
  private String amPmStart;
  private int startHour;
  private int startMinute;
  private String amPmEnd;
  private int endHour;
  private int endMinute;
  private int colorCode; // 1부터 5까지 색상 선택
  private boolean isPublic; // 공개 여부
}
