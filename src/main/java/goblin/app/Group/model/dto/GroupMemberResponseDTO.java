package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMemberResponseDTO {
  private String username;
  private String loginId;
  private String role; // 역할 필드 추가

  public GroupMemberResponseDTO(String username, String loginId, String role) {
    this.username = username;
    this.loginId = loginId;
    this.role = role; // 역할 할당
  }
}
