package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeRange {
  private String amPm; // "AM" or "PM"
  private int hour;
  private int minute;
}
