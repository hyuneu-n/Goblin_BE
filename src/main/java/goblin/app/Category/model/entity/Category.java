package goblin.app.Category.model.entity;


import goblin.app.User.model.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "false")
    private boolean deleted;

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
