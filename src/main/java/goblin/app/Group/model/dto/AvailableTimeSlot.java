package goblin.app.Group.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTimeSlot {
  private LocalDate date; // 날짜
  private LocalTime startTime; // 시작 시간
  private LocalTime endTime; // 종료 시간
}
