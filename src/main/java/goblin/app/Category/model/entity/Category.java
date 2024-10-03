package goblin.app.Category.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import goblin.app.Common.config.BooleanToYNConverter;
import goblin.app.User.model.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "categories")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Long id;

  @Column(name = "category_name", nullable = false)
  private String categoryName;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "deleted", nullable = false)
  @Convert(converter = BooleanToYNConverter.class)
  private Boolean deleted = false;

  // 색상 필드 추가
  @Column(name = "color", nullable = false)
  private String color;

  @Builder
  public Category(String categoryName, User user, String color) {
    this.categoryName = categoryName;
    this.user = user;
    this.color = color;
  }

  public void update(Long id, String categoryName, String color) {
    this.id = id;
    this.categoryName = categoryName;
    this.color = color;
  }
}
