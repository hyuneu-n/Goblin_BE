package goblin.app.Category.model.entity;

import goblin.app.Group.model.entity.Group;
import goblin.app.User.model.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CategoryVisibility {


    @JoinColumn(name = "userId")
    @OneToMany
    private User user;

    @JoinColumn(name = "groupId")
    @OneToOne
    private Group group;

    @JoinColumn(name = "categoryId")
    @OneToOne
    private Category category;
    private boolean isRevealed;

}
