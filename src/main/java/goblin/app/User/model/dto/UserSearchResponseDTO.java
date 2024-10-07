package goblin.app.User.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSearchResponseDTO {
  private String loginId;
  private String username;
}
