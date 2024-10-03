package goblin.app.Calendar.model.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
public class uCalSaveRequestDto {

  @NotBlank String title;
  String note;
  LocalDate date; // 날짜 필드 추가
  String amPmStart; // AM/PM 값
  int startHour;
  int startMinute;
  String amPmEnd; // AM/PM 값
  int endHour;
  int endMinute;

  @Builder
  public uCalSaveRequestDto(
      String title,
      String note,
      LocalDate date, // 날짜 필드 추가
      String amPmStart,
      int startHour,
      int startMinute,
      String amPmEnd,
      int endHour,
      int endMinute) {
    this.title = title;
    this.note = note;
    this.date = date; // 생성자에서 날짜 초기화
    this.amPmStart = amPmStart;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.amPmEnd = amPmEnd;
    this.endHour = endHour;
    this.endMinute = endMinute;
  }

  // 요청된 AM/PM 시간을 LocalDateTime으로 변환하는 메서드

  @JsonIgnore // 직렬화에서 제외
  public LocalDateTime getStartTime() {
    return convertToLocalDateTime(date, amPmStart, startHour, startMinute);
  }

  @JsonIgnore // 직렬화에서 제외
  public LocalDateTime getEndTime() {
    return convertToLocalDateTime(date, amPmEnd, endHour, endMinute);
  }

  private LocalDateTime convertToLocalDateTime(LocalDate date, String amPm, int hour, int minute) {
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0;
    }
    return date.atTime(hour, minute, 0, 0); // LocalDate와 시간을 결합하여 LocalDateTime 생성
  }
}
