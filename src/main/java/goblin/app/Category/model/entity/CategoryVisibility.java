package goblin.app.Category.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import goblin.app.Common.config.BooleanToYNConverter;
import goblin.app.Group.model.entity.Group;
import goblin.app.User.model.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "category_visibility")
public class CategoryVisibility {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "user_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @JoinColumn(name = "group_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Group group;

  @JoinColumn(name = "category_id")
  @OneToOne(fetch = FetchType.LAZY)
  private Category category;

  @Column(nullable = false)
  @Convert(converter = BooleanToYNConverter.class)
  private Boolean visibility = false;

  @Builder
  public CategoryVisibility(User user, Category category, Group group, Boolean visibility) {
    this.user = user;
    this.category = category;
    this.group = group;
    this.visibility = visibility;
  }

  public void update(Boolean visibility) {
    this.visibility = visibility;
  }
}
