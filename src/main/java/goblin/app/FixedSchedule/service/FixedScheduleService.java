package goblin.app.FixedSchedule.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
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
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  @Transactional
  public void createFixedSchedule(FixedScheduleRequestDTO dto, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    // 카테고리에서 색상을 받아옴
    Category category =
        categoryRepository
            .findById(dto.getCategoryId())
            .orElseThrow(
                () -> new RuntimeException("카테고리를 찾을 수 없습니다: categoryId=" + dto.getCategoryId()));

    // FixedSchedule 생성 시 dayOfWeek를 리스트로 설정
    FixedSchedule fixedSchedule =
        FixedSchedule.builder()
            .user(user)
            .category(category)
            .scheduleName(dto.getScheduleName())
            .dayOfWeek(dto.getDayOfWeek()) // 리스트로 넘김
            .startTime(
                convertTo24HourFormat(dto.getAmPmStart(), dto.getStartHour(), dto.getStartMinute()))
            .endTime(convertTo24HourFormat(dto.getAmPmEnd(), dto.getEndHour(), dto.getEndMinute()))
            .color(category.getColor()) // 카테고리에서 색상 상속
            .build();

    fixedScheduleRepository.save(fixedSchedule);
  }

  private LocalTime convertTo24HourFormat(String amPm, int hour, int minute) {
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0; // AM 12시는 0시로 처리
    }
    return LocalTime.of(hour, minute);
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
    schedule.setDayOfWeek(updateRequest.getDayOfWeek()); // 리스트로 설정
    schedule.updateTime(startTime, endTime);
  }

  // AM/PM 시간 변환 메서드
  private LocalTime convertToLocalTime(String amPm, int hour, int minute) {
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0; // AM 12시는 0시로 처리
    }
    return LocalTime.of(hour, minute);
  }

  // 색상 코드 번호에 따라 미리 지정된 색상 값을 반환하는 메서드
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
    // 로그인한 사용자를 가져옴
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    // 삭제할 고정 일정을 찾음
    FixedSchedule schedule =
        fixedScheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("고정 일정을 찾을 수 없습니다: scheduleId=" + scheduleId));

    // 일정 소유자가 현재 로그인한 사용자인지 확인
    if (!schedule.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("해당 고정 일정을 삭제할 권한이 없습니다.");
    }

    // 고정 일정 삭제
    fixedScheduleRepository.delete(schedule);
  }
}
