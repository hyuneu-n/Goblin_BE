package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class ConfirmTimeRangeRequest {
  private Long optimalTimeSlotId; // 최적 시간 슬롯의 ID
  private LocalDateTime startTime; // 사용자가 선택한 시작 시간
  private LocalDateTime endTime; // 사용자가 선택한 종료 시간
}
