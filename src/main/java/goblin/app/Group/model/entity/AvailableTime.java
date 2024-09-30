package goblin.app.Group.model.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "available_time")
public class AvailableTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long calendarId;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private LocalDateTime endTime;
}
