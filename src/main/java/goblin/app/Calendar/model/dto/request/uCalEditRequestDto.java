package goblin.app.Calendar.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class uCalEditRequestDto {

    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
