package goblin.app.Calendar.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import goblin.app.Common.config.BooleanToYNConverter;
import goblin.app.User.model.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_calendar")
public class UserCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(nullable = true)
  @Lob
  private String note;

  @Column(name = "start_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime endTime;

  @Convert(converter = BooleanToYNConverter.class)
  private Boolean deleted = false;

  @Column(name = "color", nullable = false)
  private String color;

  @Builder
  public UserCalendar(
      User user,
      String title,
      String note,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String color) {
    this.user = user;
    this.title = title;
    this.note = note;
    this.startTime = startTime;
    this.endTime = endTime;
    this.color = color;
  }

  public void update(Long id, String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
