package goblin.app.Calendar.model.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonFormat;

import goblin.app.Category.model.entity.Category;
import goblin.app.Common.config.BooleanToYNConverter;
import goblin.app.User.model.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_calendar")
public class UserCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @JoinColumn(name = "category_id", nullable = true) // Category가 없을 때 null로 허용
  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Category category;

  @Column(nullable = false)
  private String title;

  @Column(nullable = true)
  @Lob
  private String note;

  @Column(name = "start_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endTime;

  @Convert(converter = BooleanToYNConverter.class)
  private Boolean deleted = false;

  @Builder
  public UserCalendar(
      Category category,
      User user,
      String title,
      String note,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    this.category = category;
    this.user = user;
    this.title = title;
    this.note = note;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void update(Long id, String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.id = id;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
