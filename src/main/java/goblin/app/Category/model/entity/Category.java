package goblin.app.Category.model.entity;


import goblin.app.User.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private boolean isRevealed;

}
