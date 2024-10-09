package goblin.app.Group.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import goblin.app.Group.model.entity.GroupCalendar;

@Data
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

  @Builder
  public GroupCalendarResponseDTO(GroupCalendar calendar) {
    this.id = calendar.getId();
    this.groupName = calendar.getGroup().getGroupName();
    this.title = calendar.getTitle();
    this.selectedDates =
        calendar.getSelectedDates().stream().map(LocalDate::toString).collect(Collectors.toList());
    this.time = calendar.getTime();
    this.place = calendar.getPlace();
    this.link = calendar.getLink();
    this.note = calendar.getNote();
    this.confirmed = calendar.isConfirmed();
    this.createdBy = calendar.getCreatedBy().getUsername();
    this.selectedDateTimes =
        calendar.getSelectedDates().stream()
            .map(
                date ->
                    SelectedDateTimeDTO.builder()
                        .startDateTime(date.atTime(calendar.getStartTime()))
                        .endDateTime(date.atTime(calendar.getEndTime()))
                        .build())
            .collect(Collectors.toList());
  }
}
