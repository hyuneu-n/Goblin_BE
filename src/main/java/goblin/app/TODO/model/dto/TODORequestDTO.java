package goblin.app.TODO.model.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TODORequestDTO {
  private String task;
  private LocalDate dueDate;
}
