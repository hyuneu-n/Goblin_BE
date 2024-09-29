package goblin.app.Calendar.service;


import goblin.app.Calendar.model.dto.request.uCalEditRequestDto;
import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Calendar.model.entity.UserCalRepository;
import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Common.exception.CustomException;
import goblin.app.Common.exception.ErrorCode;
import goblin.app.User.model.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@Getter
public class UserCalService {

    @Autowired
    private UserCalRepository userCalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // 고정 스케줄 등록
    @Transactional
    public uCalResponseDto save (uCalSaveRequestDto requestDto, User currentUser) {
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        UserCalendar userCalendar = userCalRepository.save(requestDto.toEntity(currentUser,category));
        return new uCalResponseDto(userCalendar);
    }

    // 고정 스케줄 수정
    @Transactional
    public uCalResponseDto edit(uCalEditRequestDto requestDto, User currentUser) {
        UserCalendar userCalendar = userCalRepository.findById(requestDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 작성자인지 체크
        if (!userCalendar.getUser().equals(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 권한 없음 예외 처리
        }

        // 일정 수정
        userCalendar.update(requestDto.getId(), requestDto.getTitle(), requestDto.getStartTime(), requestDto.getEndTime());
        return new uCalResponseDto(userCalendar);
    }

    // 고정 스케줄 삭제 (hard delete)
    @Transactional
    public uCalResponseDto deleteById(Long id, User currentUser) {
        UserCalendar userCalendar = userCalRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (currentUser.equals(userCalendar.getUser()))
            userCalRepository.delete(userCalendar);
        else
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        return new uCalResponseDto(userCalendar);
    }


    @Transactional
    // 고정 스케줄 월별 조회 (페이징 x) 3개까지만
    public List<uCalResponseDto> viewByMonth(int year, int month, User user) {
        // 기본값으로 현재 시간에서 연도와 월을 가져오기
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // 매개변수 year와 month가 0 이하일 경우 기본값 사용
        year = (year <= 0) ? currentYear : year;
        month = (month <= 0 || month > 12) ? currentMonth : month;

        List<UserCalendar> scheduleList = userCalRepository.findByYearAndMonth(year, month,user);
        return scheduleList.stream()
                .limit(3)
                .map(uCalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 고정 스케줄 일별 조회
    @Transactional
    public List<uCalResponseDto> viewByDay(int year, int month, int day, User user) {
        // 기본값으로 현재 시간에서 연도와 월을 가져오기
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // 매개변수 year와 month가 0 이하일 경우 기본값 사용
        year = (year <= 0) ? currentYear : year;
        month = (month <= 0 || month > 12) ? currentMonth : month;

        List<UserCalendar> scheduleList = userCalRepository.findByDay(year, month, day,user);
        return scheduleList.stream()
                .map(uCalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 고정 스케줄 메모 등록
    // 고정 스케줄 그룹별 공유 여부 설정 (중복 제거)
    // 고정 스케줄 검색
    // 해당 카테고리 스케줄 조회

}
