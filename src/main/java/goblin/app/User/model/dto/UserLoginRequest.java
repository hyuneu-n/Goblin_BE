package goblin.app.User.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
  private String loginId;
  private String password;
}
