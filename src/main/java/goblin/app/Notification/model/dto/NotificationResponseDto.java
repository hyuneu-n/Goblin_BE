package goblin.app.Notification.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import goblin.app.Notification.model.entity.Notification;

@Getter
@NoArgsConstructor
public class NotificationResponseDto {

  private Long id;
  private String msgTitle;
  private String eventName;
  private String details1;
  private String details2;
  private String type;
  private Long calendarId;
  private Long groupId;

  public NotificationResponseDto(Notification notification) {
    this.id = notification.getId();
    this.msgTitle = notification.getMsgTitle();
    this.eventName = notification.getEventName();
    this.details1 = notification.getDetails1();
    this.details2 = notification.getDetails2();
    this.type = notification.getType().name();
    this.calendarId = notification.getCalendarId();
    this.groupId = notification.getGroupId();
  }
}
