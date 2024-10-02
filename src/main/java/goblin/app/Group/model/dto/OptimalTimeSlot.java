package goblin.app.Group.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OptimalTimeSlot {

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private List<String> participants;
}
