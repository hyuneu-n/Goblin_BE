package goblin.app.User.model.entity;


import goblin.app.Calendar.model.entity.UserCalendar;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String password;

    private String userName;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserCalendar> userCalenders;

    @Builder
    public User (String userId, String password, String userName) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
    }



}
