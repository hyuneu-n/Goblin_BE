// package goblin.app.Calendar.service;
//
//
// import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
// import goblin.app.Calendar.model.dto.response.uCalResponseDto;
// import goblin.app.Category.model.entity.Category;
// import goblin.app.Category.model.entity.CategoryRepository;
// import goblin.app.Calendar.model.entity.UserCalRepository;
// import goblin.app.Calendar.model.entity.UserCalendar;
// import goblin.app.Common.exception.CustomException;
// import goblin.app.Common.exception.ErrorCode;
// import goblin.app.User.model.entity.User;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.server.ResponseStatusException;
//
// @Service
// @NoArgsConstructor
// @Getter
// public class UserCalService {
//
//    private UserCalRepository userCalRepository;
//
//    private CategoryRepository categoryRepository;
//
//    // 고정 스케줄 등록
//    @Transactional
//    public uCalResponseDto save (uCalSaveRequestDto requestDto, User user) {
//        Category category = categoryRepository.findById(requestDto.getCategoryId())
//                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
//        UserCalendar userCalendar = userCalRepository.save(requestDto.toEntity(user,category));
//        return new uCalResponseDto(userCalendar);
//    }
//
//    // 고정 스케줄 수정
//    @Transactional
//    public uCalResponseDto edit (uCalSaveRequestDto requestDto, User user) {
//
//    }
//
//    // 고정 스케줄 메모 등록
//
//    // 고정 스케줄 그룹별 공유 여부 설정 (중복 제거)
//
//    // 고정 스케줄 월별 조회
//
//    // 고정 스케줄
//
//
// }
