package goblin.app.Group.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_calendar_participant")
@Getter
@Setter
public class GroupCalendarParticipant { // 그룹에서 일정 만들 때 해당 일정 참여자 엔티티 (그룹 멤버 아님!)

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long calendarId;

  @Column(nullable = false)
  private Long userId;
}
