package goblin.app.Group.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCalendarRequestDTO {
  private String title;
  private List<LocalDate> dates; // 여러 날짜 선택
  private TimeRange timeRange; // AM/PM, 시간과 분 정보
  private String place;
  private List<String> participants;
}
