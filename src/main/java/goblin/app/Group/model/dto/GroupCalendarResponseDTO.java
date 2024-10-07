package goblin.app.Group.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupCalendarResponseDTO {
  private Long id;
  private String groupName;
  private String title;
  private List<String> selectedDates;
  private int time;
  private String place;
  private String link;
  private String note;
  private boolean confirmed;
  private String createdBy;
  private List<SelectedDateTimeDTO> selectedDateTimes; // startDateTime, endDateTime 포함하는 리스트
}
