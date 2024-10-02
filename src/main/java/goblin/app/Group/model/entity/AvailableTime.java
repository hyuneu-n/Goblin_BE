package goblin.app.Group.model.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import goblin.app.User.model.entity.User;

@Entity
@Table(name = "available_time")
@Getter
@Setter
@NoArgsConstructor
public class AvailableTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(
      name = "login_id",
      referencedColumnName = "login_id",
      nullable = false) // User 엔티티의 loginId를 참조
  private User user;

  @Column(nullable = false)
  private Long calendarId;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private LocalDateTime endTime;
}
