package goblin.app.FixedSchedule.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.FixedSchedule.model.dto.FixedScheduleRequestDTO;
import goblin.app.FixedSchedule.model.dto.FixedScheduleResponseDTO;
import goblin.app.FixedSchedule.model.entity.FixedSchedule;
import goblin.app.FixedSchedule.repository.FixedScheduleRepository;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FixedScheduleService {

  private final FixedScheduleRepository fixedScheduleRepository;
  private final UserRepository userRepository;

  @Transactional
  public FixedScheduleResponseDTO createFixedSchedule(
      FixedScheduleRequestDTO requestDto, User user) {

    // 고정 일정 생성
    FixedSchedule fixedSchedule =
        FixedSchedule.builder()
            .scheduleName(requestDto.getScheduleName())
            .startTime(
                convertToLocalTime(
                    requestDto.getAmPmStart(),
                    requestDto.getStartHour(),
                    requestDto.getStartMinute()))
            .endTime(
                convertToLocalTime(
                    requestDto.getAmPmEnd(), requestDto.getEndHour(), requestDto.getEndMinute()))
            .dayOfWeek(requestDto.getDayOfWeek())
            .user(user)
            .color(resolveColorCode(requestDto.getColorCode())) // 사용자가 선택한 색상 설정
            .isPublic(requestDto.isPublic()) // 공개 여부 설정
            .build();

    fixedScheduleRepository.save(fixedSchedule);
    return new FixedScheduleResponseDTO(fixedSchedule);
  }

  @Transactional(readOnly = true)
  public List<FixedScheduleResponseDTO> getUserFixedSchedules(String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));
    List<FixedSchedule> schedules = fixedScheduleRepository.findByUser(user);
    return schedules.stream().map(FixedScheduleResponseDTO::new).collect(Collectors.toList());
  }

  @Transactional
  public void updateFixedSchedule(
      Long scheduleId, FixedScheduleRequestDTO updateRequest, String loginId) {
    // 일정 찾기
    FixedSchedule schedule =
        fixedScheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: scheduleId=" + scheduleId));

    // AM/PM 시간 변환
    LocalTime startTime =
        convertToLocalTime(
            updateRequest.getAmPmStart(),
            updateRequest.getStartHour(),
            updateRequest.getStartMinute());
    LocalTime endTime =
        convertToLocalTime(
            updateRequest.getAmPmEnd(), updateRequest.getEndHour(), updateRequest.getEndMinute());

    // 요일 리스트와 시간 업데이트
    schedule.setScheduleName(updateRequest.getScheduleName());
    schedule.setDayOfWeek(updateRequest.getDayOfWeek());
    schedule.updateTime(startTime, endTime);
    schedule.setColor(resolveColorCode(updateRequest.getColorCode())); // 색상 변경 가능
    schedule.setPublic(updateRequest.isPublic()); // 공개 여부 업데이트

    fixedScheduleRepository.save(schedule);
  }

  private LocalTime convertToLocalTime(String amPm, int hour, int minute) {
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0; // AM 12시는 0시로 처리
    }
    return LocalTime.of(hour, minute);
  }

  private String resolveColorCode(int colorCode) {
    switch (colorCode) {
      case 1:
        return "F3DAD8";
      case 2:
        return "F1DAED";
      case 3:
        return "F2EDD9";
      case 4:
        return "E6E8E3";
      case 5:
        return "B1B0B5";
      default:
        throw new IllegalArgumentException("유효하지 않은 색상 코드입니다: " + colorCode);
    }
  }

  @Transactional
  public void deleteFixedSchedule(Long scheduleId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    FixedSchedule schedule =
        fixedScheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("고정 일정을 찾을 수 없습니다: scheduleId=" + scheduleId));

    if (!schedule.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("해당 고정 일정을 삭제할 권한이 없습니다.");
    }

    fixedScheduleRepository.delete(schedule);
  }
}
