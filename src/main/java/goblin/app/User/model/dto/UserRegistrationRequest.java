package goblin.app.User.model.dto;

import lombok.Getter;
import lombok.Setter;

/*
 * UserRegistrationRequest: 회원가입 요청 데이터를 담고 있는 DTO 클래스
 */
@Getter
@Setter
public class UserRegistrationRequest {
  private String username;
  private String loginId;
  private String password;
}
