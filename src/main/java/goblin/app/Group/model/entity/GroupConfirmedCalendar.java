package goblin.app.Group.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_confirmed_calendar")
@Getter
@Setter
@NoArgsConstructor
public class GroupConfirmedCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private Long calendarId;

  @Column(nullable = false)
  private LocalDateTime confirmedStartTime;

  @Column(nullable = false)
  private LocalDateTime confirmedEndTime;

  private String title;
  private String place;
  private String note;

  @Builder
  public GroupConfirmedCalendar(
      Long groupId,
      Long calendarId,
      LocalDateTime confirmedStartTime,
      LocalDateTime confirmedEndTime,
      String title,
      String place,
      String note) {
    this.groupId = groupId;
    this.calendarId = calendarId;
    this.confirmedStartTime = confirmedStartTime;
    this.confirmedEndTime = confirmedEndTime;
    this.title = title;
    this.place = place;
    this.note = note;
  }
}
