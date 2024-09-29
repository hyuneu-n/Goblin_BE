package goblin.app.Category.model.entity;

import goblin.app.Group.model.entity.Group;
import goblin.app.User.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CategoryVisibility {


    @JoinColumn(name = "user_id")
    @OneToMany
    private User user;

    @JoinColumn(name = "group_id")
    @OneToOne
    private Group group;

    @JoinColumn(name = "category_id")
    @OneToOne
    private Category category;

    @Column(columnDefinition = "false")
    private boolean isRevealed;


}
