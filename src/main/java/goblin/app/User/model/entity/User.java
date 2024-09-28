package goblin.app.User.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  // 회원 id
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // 로그인 id
  @Column(name = "login_id", unique = true)
  private String loginId;
  // login pw
  @Column(name = "login_pw")
  private String loginPw;

  @Column(name = "name", unique = true)
  private String username;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "role")
  private String userRole = "ROLE_USER";
}
