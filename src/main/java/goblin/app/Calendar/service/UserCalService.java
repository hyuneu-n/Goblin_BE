package goblin.app.Calendar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.Calendar.model.dto.request.uCalEditRequestDto;
import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Calendar.model.entity.UserCalRepository;
import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Calendar.repository.UserCalendarRepository;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Common.exception.CustomException;
import goblin.app.Common.exception.ErrorCode;
import goblin.app.User.model.entity.User;

@Service
@RequiredArgsConstructor
public class UserCalService {

  private final UserCalRepository userCalRepository;
  private final CategoryRepository categoryRepository;
  private final UserCalendarRepository userCalendarRepository;

  // 일반 스케쥴 등록
  @Transactional
  public uCalResponseDto save(uCalSaveRequestDto requestDto, User currentUser) {
    Category category =
        categoryRepository
            .findById(requestDto.getCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

    UserCalendar userCalendar = userCalRepository.save(requestDto.toEntity(currentUser, category));
    return new uCalResponseDto(userCalendar);
  }

  // 개인 일반 스케줄 수정
  @Transactional
  public uCalResponseDto edit(uCalEditRequestDto requestDto, User currentUser) {
    UserCalendar userCalendar =
        userCalRepository
            .findById(requestDto.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

    // 작성자인지 체크
    validateUser(userCalendar, currentUser);

    // 일정 수정
    userCalendar.update(
        requestDto.getId(),
        requestDto.getTitle(),
        requestDto.getStartTime(),
        requestDto.getEndTime());
    return new uCalResponseDto(userCalendar);
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

  // 개인 스케줄 월별 조회 (3개까지만 조회)
  @Transactional
  public List<uCalResponseDto> viewByMonth(int year, int month, User user) {
    int[] yearMonth = validateYearAndMonth(year, month);
    List<UserCalendar> scheduleList =
        userCalRepository.findByYearAndMonth(yearMonth[0], yearMonth[1], user);

    return scheduleList.stream().limit(3).map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 개인 스케줄 일별 조회
  @Transactional
  public List<uCalResponseDto> viewByDay(int year, int month, int day, User user) {
    int[] yearMonth = validateYearAndMonth(year, month);
    List<UserCalendar> scheduleList =
        userCalRepository.findByDay(yearMonth[0], yearMonth[1], day, user);

    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 고정 스케줄 메모 등록 (추가)
  /*
  @Transactional
  public void addMemo(Long scheduleId, String memo, User currentUser) {
      UserCalendar userCalendar = userCalRepository.findById(scheduleId)
              .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

      validateUser(userCalendar, currentUser);
      userCalendar.addMemo(memo); // 메모 추가
  }
   */

  // 카테고리별 스케줄 조회
  @Transactional
  public List<uCalResponseDto> viewByCategory(Long categoryId, User currentUser) {
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

    List<UserCalendar> scheduleList =
        userCalRepository.findByCategoryAndUser(category, currentUser);
    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 개인 스케줄 검색 기능
  @Transactional
  public List<uCalResponseDto> searchSchedules(String keyword, User currentUser) {
    List<UserCalendar> scheduleList =
        userCalRepository.findByTitleContainingAndUser(keyword, currentUser);
    return scheduleList.stream().map(uCalResponseDto::new).collect(Collectors.toList());
  }

  // 작성자 검증 메서드
  private void validateUser(UserCalendar userCalendar, User currentUser) {
    if (!userCalendar.getUser().equals(currentUser)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 권한 없음 예외 처리
    }
  }

  // 연도와 월 검증 및 기본값 설정
  private int[] validateYearAndMonth(int year, int month) {
    int currentYear = LocalDate.now().getYear();
    int currentMonth = LocalDate.now().getMonthValue();

    year = (year <= 0) ? currentYear : year;
    month = (month <= 0 || month > 12) ? currentMonth : month;

    return new int[] {year, month};
  }

  public void save(uCalSaveRequestDto requestDto, User user, Category category) {
    UserCalendar userCalendar = requestDto.toEntity(user, category);
    userCalendarRepository.save(userCalendar);
  }
}
