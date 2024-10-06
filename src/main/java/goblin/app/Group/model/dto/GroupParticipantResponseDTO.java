package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupParticipantResponseDTO {
  private String username;
  private boolean isAvailableTimeSubmitted;

  public GroupParticipantResponseDTO(String username, boolean isAvailableTimeSubmitted) {
    this.username = username;
    this.isAvailableTimeSubmitted = isAvailableTimeSubmitted;
  }
}
