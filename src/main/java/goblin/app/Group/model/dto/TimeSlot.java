package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimeSlot {
  private LocalDateTime startTime;
  private LocalDateTime endTime;
}
