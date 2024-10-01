package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmEventRequestDTO {
  private LocalDateTime startTime; // 선택된 시작 시간
  private LocalDateTime endTime; // 선택된 종료 시간
}
