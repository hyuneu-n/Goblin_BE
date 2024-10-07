package goblin.app.Notification.model.entity;

import lombok.Builder;
import lombok.Getter;

import jakarta.persistence.*;

import goblin.app.User.model.entity.User;

@Entity
@Getter
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long id;

  @Column(name = "msg_title", nullable = false)
  private String msgTitle;

  @Column(name = "event_name", nullable = false)
  private String eventName;

  private String details1;
  private String details2;

  @JoinColumn(name = "receiver_id")
  @ManyToOne
  private User user;

  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Builder
  public Notification(
      Long id,
      String msgTitle,
      String eventName,
      String details1,
      String details2,
      User user,
      NotificationType type) {
    this.id = id;
    this.msgTitle = msgTitle;
    this.eventName = eventName;
    this.details1 = details1;
    this.details2 = details2;
    this.user = user;
    this.type = type;
  }
}
