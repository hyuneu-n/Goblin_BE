package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeRange {
  private String startAmPm;
  private int startHour;
  private int startMinute;
  private String endAmPm;
  private int endHour;
  private int endMinute;
}
