package goblin.app.User.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
  private String accessToken;
  private String refreshToken;
  private String loginId; // 로그인 ID 추가
  private String username;
  private Long personalGroupId; // 개인 그룹 ID 추가

  // 생성자
  public AuthResponse(String accessToken) {
    this.accessToken = accessToken;
  }

  public AuthResponse(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  // 새 생성자: 로그인 ID, 닉네임, 개인 그룹 ID를 포함
  public AuthResponse(
      String accessToken,
      String refreshToken,
      String loginId,
      String username,
      Long personalGroupId) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.loginId = loginId;
    this.username = username;
    this.personalGroupId = personalGroupId;
  }
}
