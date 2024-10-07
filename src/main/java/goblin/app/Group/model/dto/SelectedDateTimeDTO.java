package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectedDateTimeDTO {
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
}
