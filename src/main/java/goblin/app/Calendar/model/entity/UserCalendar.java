package goblin.app.Calendar.model.entity;

import goblin.app.Category.model.entity.Category;
import goblin.app.User.model.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class UserCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinColumn(name = "userId")
    private User user;

    @JoinColumn(name = "categoryId")
    @OneToMany
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    @Lob
    private String note;

    private LocalDateTime startTime;

    private LocalDateTime endTime;


    @Builder
    public UserCalendar(Category category, User user, String title, String note, LocalDateTime startTime, LocalDateTime endTime){
        this.category = category;
        this.user = user;
        this.title = title;
        this.note = note;
        this.startTime = startTime;
        this.endTime = endTime;
    }



}
