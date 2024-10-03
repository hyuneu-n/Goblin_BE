package goblin.app.Group.model.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCalendarRequestDTO {

  private String title;
  private List<LocalDate> dates; // 선택된 날짜 리스트
  private Integer duration; // 소요 시간
  private TimeRange timeRange; // 모든 날짜에 적용될 시간 범위
  private String place;
  private String link;
  private String note;
  private List<String> participants;
}
