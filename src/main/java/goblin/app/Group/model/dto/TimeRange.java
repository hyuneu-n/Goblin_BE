package goblin.app.Group.model.dto;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeRange {
  private String amPm;
  private int hour;
  private int minute;

  public LocalTime toLocalTime() {
    int convertedHour = hour;
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      convertedHour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      convertedHour = 0;
    }
    return LocalTime.of(convertedHour, minute);
  }
}
