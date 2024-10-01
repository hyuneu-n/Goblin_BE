package goblin.app.Group.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCalendarRequestDTO {
  private String title;
  private List<LocalDateTime> dates; // 각 날짜 목록
  private LocalDateTime startDate; // 시작 날짜
  private LocalDateTime endDate; // 종료 날짜
  private TimeRange timeRange; // AM/PM, 시간과 분 정보
  private String place;
  private String link;
  private String note;
  private List<String> participants;
}
