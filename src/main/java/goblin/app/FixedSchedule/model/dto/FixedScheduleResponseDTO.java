package goblin.app.FixedSchedule.model.dto;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import goblin.app.FixedSchedule.model.entity.FixedSchedule;

@Setter
@Getter
@NoArgsConstructor
public class FixedScheduleResponseDTO {
  private Long id;
  private String scheduleName;
  private String startTime;
  private String endTime;
  private List<String> dayOfWeek; // 혹은 DayOfWeek 열거형

  public FixedScheduleResponseDTO(FixedSchedule schedule) {
    this.id = schedule.getId();
    this.scheduleName = schedule.getScheduleName();
    this.startTime = schedule.getStartTime().toString();
    this.endTime = schedule.getEndTime().toString();
    this.dayOfWeek =
        schedule.getDayOfWeek().stream().map(DayOfWeek::name).collect(Collectors.toList());
  }
}
