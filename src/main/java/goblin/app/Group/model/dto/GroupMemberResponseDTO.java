package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMemberResponseDTO {
  private String username;
  private String loginId;

  public GroupMemberResponseDTO(String username, String loginId) {
    this.username = username;
    this.loginId = loginId;
  }
}
