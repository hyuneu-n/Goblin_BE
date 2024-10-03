package goblin.app.Category.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goblin.app.Category.model.dto.request.categoryEditRequestDto;
import goblin.app.Category.model.dto.request.categorySaveRequestDto;
import goblin.app.Category.model.dto.request.categoryViEditRequestDto;
import goblin.app.Category.model.dto.response.categoryResponseDto;
import goblin.app.Category.model.dto.response.categoryVisibilityResponseDto;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Category.model.entity.CategoryVisibility;
import goblin.app.Category.model.entity.CategoryVisibilityRepository;
import goblin.app.Common.exception.CustomException;
import goblin.app.Common.exception.ErrorCode;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.User.model.entity.User;

@Service
@NoArgsConstructor
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class CategoryService {

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private CategoryVisibilityRepository categoryVisibilityRepository;

  // 카테고리 추가 (색상 코드 적용)
  @Transactional
  public categoryResponseDto save(categorySaveRequestDto requestDto, User user) {
    String color = resolveColorCode(requestDto.getColorCode()); // 색상 코드에 따른 색상값 설정
    Category category =
        Category.builder()
            .categoryName(requestDto.getCategoryName())
            .user(user)
            .color(color) // 색상값 저장
            .build();

    categoryRepository.save(category);
    return new categoryResponseDto(category);
  }

  // 카테고리 수정
  @Transactional
  public categoryResponseDto edit(categoryEditRequestDto requestDto, User user) {
    Category category =
        categoryRepository
            .findById(requestDto.getCategoryId()) // categoryId를 사용하여 카테고리 조회
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

    if (!category.getUser().equals(user)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 색상 코드를 String 값으로 변환
    String resolvedColor = requestDto.resolveColorCode();

    // 카테고리 업데이트
    category.update(requestDto.getCategoryId(), requestDto.getCategoryName(), resolvedColor);

    return new categoryResponseDto(category);
  }

  // 카테고리 삭제 - soft (생성한 사용자만), 사용자 고정 일정도 삭제됨
  @Transactional
  public categoryResponseDto deleteById(Long id, User user) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    if (!category.getUser().equals(user)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
    // 소프트 삭제 수행
    categoryRepository.deleteById(id);
    return new categoryResponseDto(category);
  }

  // 카테고리 목록 조회 (삭제되지 않은 항목들만)
  @Transactional
  public List<categoryResponseDto> viewAll(User user) {
    List<Category> categories = categoryRepository.findAll();
    return categories.stream().map(categoryResponseDto::new).collect(Collectors.toList());
  }

  // 카테고리 개별 조회
  @Transactional
  public categoryResponseDto getCategoryById(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    return new categoryResponseDto(category);
  }

  @Transactional
  public categoryVisibilityResponseDto setCategoryVisibility(
      categoryViEditRequestDto requestDto, User user) {
    // 카테고리 존재 여부 확인
    Category category =
        categoryRepository
            .findById(requestDto.getCategoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

    // 그룹 존재 여부 확인
    Group group =
        groupRepository
            .findById(requestDto.getGroupId())
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

    // 카테고리와 그룹에 대한 visibility 정보 조회 또는 새로 생성
    CategoryVisibility categoryVisibility =
        categoryVisibilityRepository
            .findByCategoryAndGroupAndUser(category, group, user)
            .orElseGet(
                () -> {
                  // 새로 생성 시 카테고리 정보 포함
                  return requestDto.toEntity(category, user, group); // 카테고리와 그룹 포함하여 생성
                });

    // visibility 값을 업데이트
    categoryVisibility.update(requestDto.getVisibility());
    categoryVisibilityRepository.save(categoryVisibility);

    return new categoryVisibilityResponseDto(categoryVisibility);
  }

  // 카테고리 그룹별 on off 여부를 조회 (유저,그룹 정보로 조회) -> 카테고리, 공개 여부를 각각 반환함
  @Transactional(readOnly = true)
  public List<categoryVisibilityResponseDto> getCategoryVisibilityByGroup(User user, Long groupId) {
    // 그룹 존재 여부 확인
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

    // 해당 그룹에 대한 모든 카테고리 가시성 조회
    List<CategoryVisibility> visibilityList =
        categoryVisibilityRepository.findByGroupAndUser(group, user);

    // 가시성 정보를 DTO 리스트로 변환
    List<categoryVisibilityResponseDto> responseDtos =
        visibilityList.stream()
            .map(categoryVisibilityResponseDto::new)
            .collect(Collectors.toList());

    return responseDtos;
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
}
