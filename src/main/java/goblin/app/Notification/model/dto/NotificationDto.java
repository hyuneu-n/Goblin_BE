package goblin.app.Notification.model.dto;

import lombok.*;
import goblin.app.Notification.model.entity.Notification;
import goblin.app.Notification.model.entity.NotificationType;
import goblin.app.User.model.entity.User;

@Data
@NoArgsConstructor
@Getter
public class NotificationDto {

  private String msgTitle; // 메시지 제목
  private String eventName; // 이벤트 이름
  private String details1; // 세부 사항 1
  private String details2; // 세부 사항 2
  private NotificationType type; // 알림 유형

  private Long calendarId;
  private Long groupId;

  @Builder
  private NotificationDto(
      String msgTitle,
      String eventName,
      String details1,
      String details2,
      NotificationType type,
      Long calendarId,
      Long groupId) {
    this.msgTitle = msgTitle;
    this.eventName = eventName;
    this.details1 = details1;
    this.details2 = details2;
    this.type = type;
    this.calendarId = calendarId;
    this.groupId = groupId;
  }

  public Notification ToEntity(User user) {
    return Notification.builder()
        .user(user)
        .eventName(eventName)
        .msgTitle(msgTitle)
        .details1(details1)
        .details2(details2)
        .type(type)
        .build();
  }
}
