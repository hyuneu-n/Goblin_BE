package goblin.app.Calendar.model.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class uCalRequestDto {

  @NotBlank String title;
  String note;
  List<LocalDate> date; // 여러 날짜 처리
  String amPmStart; // AM/PM 값
  int startHour;
  int startMinute;
  String amPmEnd; // AM/PM 값
  int endHour;
  int endMinute;

  @Builder
  public uCalRequestDto(
      String title,
      String note,
      List<LocalDate> date,
      String amPmStart,
      int startHour,
      int startMinute,
      String amPmEnd,
      int endHour,
      int endMinute) {
    this.title = title;
    this.note = note;
    this.date = date; // 날짜 초기화
    this.amPmStart = amPmStart;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.amPmEnd = amPmEnd;
    this.endHour = endHour;
    this.endMinute = endMinute;
  }
}
