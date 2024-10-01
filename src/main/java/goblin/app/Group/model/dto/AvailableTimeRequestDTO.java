package goblin.app.Group.model.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTimeRequestDTO {
  private List<AvailableTimeSlot> availableTimeSlots; // 여러 날짜와 시간 범위를 받을 수 있도록 함
  private String loginId;
}
