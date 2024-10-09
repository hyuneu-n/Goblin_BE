package goblin.app.Calendar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.Calendar.model.dto.request.uCalRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Calendar.model.entity.UserCalRepository;
import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Common.exception.CustomException;
import goblin.app.Common.exception.ErrorCode;
import goblin.app.User.model.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCalService {

  private final UserCalRepository userCalRepository;

  // 일정 등록
  @Transactional
  public List<uCalResponseDto> save(uCalRequestDto requestDto, User currentUser) {
    List<LocalDate> dates = requestDto.getDate(); // 여러 날짜를 가져옴
    List<UserCalendar> calendars = new ArrayList<>();

    for (LocalDate date : dates) {
      LocalDateTime startTime =
          convertToLocalDateTime(
              date,
              requestDto.getAmPmStart(),
              requestDto.getStartHour(),
              requestDto.getStartMinute());
      LocalDateTime endTime =
          convertToLocalDateTime(
              date, requestDto.getAmPmEnd(), requestDto.getEndHour(), requestDto.getEndMinute());

      UserCalendar userCalendar =
          UserCalendar.builder()
              .title(requestDto.getTitle())
              .note(requestDto.getNote())
              .user(currentUser)
              .startTime(startTime)
              .endTime(endTime)
              .color("CCD7E5") // 색상 고정
              .build();

      calendars.add(userCalRepository.save(userCalendar));
    }

    return calendars.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 일정 수정
  @Transactional
  public List<uCalResponseDto> edit(Long scheduleId, uCalRequestDto requestDto, User currentUser) {
    UserCalendar userCalendar =
        userCalRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

    // 작성자 확인
    validateUser(userCalendar, currentUser);

    List<LocalDate> dates = requestDto.getDate();

    // 일정 수정 처리
    for (LocalDate date : dates) {
      LocalDateTime startTime =
          convertToLocalDateTime(
              date,
              requestDto.getAmPmStart(),
              requestDto.getStartHour(),
              requestDto.getStartMinute());
      LocalDateTime endTime =
          convertToLocalDateTime(
              date, requestDto.getAmPmEnd(), requestDto.getEndHour(), requestDto.getEndMinute());

      userCalendar.update(scheduleId, requestDto.getTitle(), startTime, endTime);
    }

    return List.of(new uCalResponseDto(userCalendar));
  }

  // 작성자 검증 로직
  private void validateUser(UserCalendar userCalendar, User currentUser) {
    log.info(
        "userCalendar 작성자: {}, 현재 사용자: {}",
        userCalendar.getUser().getLoginId(),
        currentUser.getLoginId());
    if (!userCalendar.getUser().getLoginId().equals(currentUser.getLoginId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  // 개인 스케줄 월별 조회 (3개까지만 조회)
  @Transactional
  public List<uCalResponseDto> viewByMonth(int year, int month, User user) {
    int[] yearMonth = validateYearAndMonth(year, month);
    List<UserCalendar> scheduleList =
        userCalRepository.findByYearAndMonth(yearMonth[0], yearMonth[1], user);

    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 개인 스케줄 일별 조회
  @Transactional
  public List<uCalResponseDto> viewByDay(int year, int month, int day, User user) {
    int[] yearMonth = validateYearAndMonth(year, month);
    List<UserCalendar> scheduleList =
        userCalRepository.findByDay(yearMonth[0], yearMonth[1], day, user);

    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 개인 일반 스케줄 삭제 (hard delete)
  @Transactional
  public uCalResponseDto deleteById(Long id, User currentUser) {
    UserCalendar userCalendar =
        userCalRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

    // 작성자인지 체크
    validateUser(userCalendar, currentUser);

    userCalRepository.delete(userCalendar);
    return new uCalResponseDto(userCalendar);
  }

  // 개인 스케줄 검색 기능
  @Transactional
  public List<uCalResponseDto> searchSchedules(String keyword, User currentUser) {
    List<UserCalendar> scheduleList =
        userCalRepository.findByTitleContainingAndUserAndDeletedFalse(keyword, currentUser);
    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 연도와 월 검증 및 기본값 설정
  private int[] validateYearAndMonth(int year, int month) {
    int currentYear = LocalDate.now().getYear();
    int currentMonth = LocalDate.now().getMonthValue();

    year = (year <= 0) ? currentYear : year;
    month = (month <= 0 || month > 12) ? currentMonth : month;

    return new int[] {year, month};
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
