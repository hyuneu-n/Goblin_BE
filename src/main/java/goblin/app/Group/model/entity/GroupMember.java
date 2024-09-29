package goblin.app.Group.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import goblin.app.User.model.entity.User;

@Entity
@Table(name = "group_members")
@Getter
@Setter
public class GroupMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_login_id", referencedColumnName = "login_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private String role;
}
