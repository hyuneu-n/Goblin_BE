package goblin.app.Group.model.dto;

import goblin.app.Group.model.entity.GroupConfirmedCalendar;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GroupConfimedCalendarRequestDTO {

    private Long groupId;
    private Long calendarId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String title;
    private String place;
    private String note;


    // 인자를 받는 생성자 추가
    @Builder
    public GroupConfimedCalendarRequestDTO(
            Long groupId,
            Long calendarId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String title,
            String place,
            String note) {
        this.groupId = groupId;
        this.calendarId = calendarId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.title = title;
        this.place = place;
        this.note = note;
    }

    public GroupConfirmedCalendar toEntity() {
        return GroupConfirmedCalendar.builder()
                .calendarId(calendarId)
                .groupId(groupId)
                .confirmedStartTime(startDateTime)
                .confirmedEndTime(endDateTime)
                .title(title)
                .place(place)
                .note(note)
                .build();
    }

}
