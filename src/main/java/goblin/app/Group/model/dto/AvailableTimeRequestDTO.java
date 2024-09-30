package goblin.app.Group.model.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTimeRequestDTO {
  private LocalDateTime startTime; // 가능한 시작 시간
  private LocalDateTime endTime; // 가능한 끝나는 시간
  private String loginId; // 참여자 로그인 ID
}
