package goblin.app.User.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRegistrationResponseDTO {
  private String username;
  private String loginId;
  private String message; // 회원가입 성공 메시지
}
