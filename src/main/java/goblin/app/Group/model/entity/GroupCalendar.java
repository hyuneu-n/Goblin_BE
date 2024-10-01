package goblin.app.Group.model.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import goblin.app.User.model.entity.User;

@Entity
@Table(name = "group_calendar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private String title;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = false)
  private List<LocalDateTime> selectedDates; // 선택된 날짜들

  @Column(nullable = false)
  private Integer time; // 예상 소요 시간

  @Column(nullable = false)
  private String place;

  @Column private String link;

  @Column(nullable = false)
  private LocalDateTime createdDate;

  @Column private String note;

  @Column(nullable = false)
  private boolean deleted = false; // Soft delete 플래그

  @Column(nullable = false)
  private boolean confirmed = false; // 일정 확정 여부

  @ManyToOne
  @JoinColumn(name = "created_by", referencedColumnName = "login_id", nullable = false)
  private User createdBy;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;
}
