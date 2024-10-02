package goblin.app.Calendar.model.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class uCalEditRequestDto {

  private Long id;

  @NotBlank private String title;

  private LocalDateTime startTime;
  private LocalDateTime endTime;
}
