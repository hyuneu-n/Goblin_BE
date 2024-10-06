package goblin.app.TODO.model.dto;

import java.time.LocalDate;

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
  private LocalDate createdDate;
  private LocalDate dueDate;
  private boolean completed;
  private int dDay;
  private String groupName;
}
