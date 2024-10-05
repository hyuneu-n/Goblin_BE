package goblin.app.TODO.model.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import goblin.app.Group.model.entity.Group;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "todos")
public class TODO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", referencedColumnName = "groupId", nullable = false)
  private Group group;

  @Column(nullable = false, length = 100)
  private String task;

  @Column(nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false)
  private boolean completed;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDate createdDate;

  public int calculateDDay() {
    return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
  }

  public void markAsCompleted() {
    this.completed = true;
  }

  public void updateTask(String newTask, LocalDate newDueDate) {
    this.task = newTask;
    this.dueDate = newDueDate;
  }
}
