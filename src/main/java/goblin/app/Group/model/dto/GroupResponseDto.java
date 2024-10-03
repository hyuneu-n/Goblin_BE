package goblin.app.Group.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupResponseDto {

  private Long groupId;
  private String groupName;
  private String createdBy; // 생성자 이름

  public GroupResponseDto(Long groupId, String groupName, String createdBy) {
    this.groupId = groupId;
    this.groupName = groupName;
    this.createdBy = createdBy;
  }
}
