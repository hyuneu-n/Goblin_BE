package goblin.app.Group.model.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTimeSlot {
  private LocalDate date; // 날짜
  private String startTime; // 시작 시간
  private String endTime; // 종료 시간
}
