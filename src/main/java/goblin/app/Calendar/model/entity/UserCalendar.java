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
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "category_id")
    @OneToMany
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    @Lob
    private String note;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
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

    public void update(Long id,String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }


}
