package goblin.app.Category.model.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import goblin.app.Calendar.model.entity.UserCalendar;
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

  @Column(name = "category_name")
  private String categoryName;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserCalendar> userCalendars = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "deleted", nullable = false)
  @Convert(converter = BooleanToYNConverter.class)
  private Boolean deleted = false;

  @Builder
  public Category(String categoryName, User user) {
    this.categoryName = categoryName;
    this.user = user;
  }

  public void update(Long id, String categoryName) {
    this.id = id;
    this.categoryName = categoryName;
  }
}
