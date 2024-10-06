package goblin.app.Group.model.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmTimeRangeRequest {
  private Long optimalTimeSlotId;
  private LocalDate date; // 날짜
  private String startAmPm; // 시작 시간 AM/PM
  private int startHour; // 시작 시간 (시)
  private int startMinute; // 시작 시간 (분)
  private String endAmPm; // 종료 시간 AM/PM
  private int endHour; // 종료 시간 (시)
  private int endMinute; // 종료 시간 (분)
}
