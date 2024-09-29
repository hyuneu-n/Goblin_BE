package goblin.app.Group.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import goblin.app.User.model.entity.User;

@Entity
@Table(name = "user_groups")
@Getter
@Setter
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long groupId;

  @Column(nullable = false)
  private String groupName;

  @ManyToOne
  @JoinColumn(name = "created_by", referencedColumnName = "login_Id", nullable = false)
  private User createdBy;

}
