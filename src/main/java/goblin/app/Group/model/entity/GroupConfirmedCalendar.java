package goblin.app.Group.model.entity;

import java.time.LocalDateTime;

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

  // 시간블럭 색상코드
  @Column(name = "color", nullable = false)
  private String color = "A5B4DB";
}
