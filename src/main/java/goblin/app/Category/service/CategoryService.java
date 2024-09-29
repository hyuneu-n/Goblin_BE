package goblin.app.Category.service;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Category.model.dto.request.categoryEditRequestDto;
import goblin.app.Category.model.dto.request.categorySaveRequestDto;
import goblin.app.Category.model.dto.response.categoryResponseDto;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Common.exception.CustomException;
import goblin.app.Common.exception.ErrorCode;
import goblin.app.User.model.entity.User;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")

public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;


    // 카테고리 추가
    @Transactional
    public categoryResponseDto save(categorySaveRequestDto requestDto, User user) {
        Category category = categoryRepository.save(requestDto.toEntity(user));
        return new categoryResponseDto(category);
    }

    // 카테고리 수정
    @Transactional
    public categoryResponseDto edit(categoryEditRequestDto requestDo, User user){
        Category category = categoryRepository.findById(requestDo.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        if (!category.getUser().equals(user)){
            throw  new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return new categoryResponseDto(category);
    }


    // 카테고리 삭제 - soft (생성한 사용자만)
    @Transactional
    public categoryResponseDto deleteById(Long id, User user){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        if (!category.getUser().equals(user)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        // 소프트 삭제 수행
        categoryRepository.deleteById(id);
        return new categoryResponseDto(category);
    }

    // 카테고리 목록 조회 (삭제되지 않은 항목들만)
    @Transactional
    public List<categoryResponseDto> viewAll(User user){
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryResponseDto::new)
                .collect(Collectors.toList());
    }

    // 카테고리 개별 조회
    @Transactional
    public categoryResponseDto getCategoryById(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        return new categoryResponseDto(category);
    }


    // 카테고리 그룹별 on off


}

