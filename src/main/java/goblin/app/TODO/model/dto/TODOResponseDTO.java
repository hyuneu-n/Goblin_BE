package goblin.app.TODO.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TODOResponseDTO {
  private Long id;
  private String task;
  private LocalDateTime createdDate; // LocalDateTime으로 변경
  private LocalDateTime dueDate; // LocalDateTime으로 변경
  private boolean completed;
  private int dDay;
  private String groupName;
}
