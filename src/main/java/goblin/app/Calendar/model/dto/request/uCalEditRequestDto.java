package goblin.app.Calendar.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class uCalEditRequestDto {

    private Long id;
    private Long userId;

    @NotBlank
    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
