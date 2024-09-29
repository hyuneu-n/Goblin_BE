package goblin.app.Group.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_calendar")
@Getter
@Setter
public class GroupCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalTime time;

  @Column(nullable = false)
  private String place;

  @Column private String link;

  @Column(nullable = false)
  private LocalDateTime createdDate;

  @Column private String note;
}
