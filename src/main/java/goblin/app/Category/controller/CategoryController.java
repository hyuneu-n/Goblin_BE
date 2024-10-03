package goblin.app.Category.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goblin.app.Category.model.dto.request.categoryEditRequestDto;
import goblin.app.Category.model.dto.request.categorySaveRequestDto;
import goblin.app.Category.model.dto.request.categoryViEditRequestDto;
import goblin.app.Category.model.dto.response.categoryResponseDto;
import goblin.app.Category.model.dto.response.categoryVisibilityResponseDto;
import goblin.app.Category.service.CategoryService;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/calendar/user/category")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

  @Autowired private final CategoryService categoryService;
  @Autowired private final JwtUtil jwtUtil;
  @Autowired private final UserRepository userRepository;

  // 카테고리 저장
  @PostMapping("/save")
  @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 저장합니다.")
  public ResponseEntity<categoryResponseDto> save(
      @RequestBody categorySaveRequestDto requestDto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      categoryResponseDto responseDto = categoryService.save(requestDto, user);
      return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    } catch (RuntimeException e) {
      log.error("카테고리 저장 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 카테고리 수정
  @PutMapping("/edit")
  @Operation(summary = "카테고리 수정", description = "카테고리를 수정합니다.")
  public ResponseEntity<categoryResponseDto> edit(
      @RequestBody @Valid categoryEditRequestDto requestDto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      categoryResponseDto responseDto = categoryService.edit(requestDto, user);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    } catch (RuntimeException e) {
      log.error("카테고리 수정 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 카테고리 삭제
  @DeleteMapping("/delete/{categoryId}")
  @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
  public ResponseEntity<String> delete(
      @PathVariable Long categoryId,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      categoryService.deleteById(categoryId, user);
      return ResponseEntity.status(HttpStatus.OK).body("카테고리가 성공적으로 삭제되었습니다.");
    } catch (RuntimeException e) {
      log.error("카테고리 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
    }
  }

  // 카테고리 가시성 설정
  @PutMapping("/visibility/edit")
  @Operation(summary = "카테고리 공개 여부 설정", description = "그룹별 고정 일정 카테고리의 공개 여부를 설정합니다.")
  public ResponseEntity<categoryVisibilityResponseDto> setCategoryVisibility(
      @RequestBody @Valid categoryViEditRequestDto requestDto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      categoryVisibilityResponseDto responseDto =
          categoryService.setCategoryVisibility(requestDto, user);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    } catch (RuntimeException e) {
      log.error("카테고리 가시성 설정 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/visibility/view")
  @Operation(summary = "카테고리 공개 여부 조회", description = "그룹별 고정 일정 카테고리의 공개 여부를 조회")
  public ResponseEntity<List<categoryVisibilityResponseDto>> getCategoryVisibility(
      @RequestParam Long groupId,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      List<categoryVisibilityResponseDto> responseDto =
          categoryService.getCategoryVisibilityByGroup(user, groupId);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    } catch (RuntimeException e) {
      log.error("그룹별 카테고리 고정일정 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // JWT 토큰에서 User 객체 추출하는 메서드
  private User getUserFromToken(String bearerToken) {
    String loginId = null;

    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);

      // JWT에서 loginId를 추출하는 부분
      loginId = claims.getId(); // 보통 sub 필드에 loginId가 저장됨

      log.info("JWT Claims: {}", claims); // claims 전체 구조 확인
      log.info("추출된 로그인 ID: {}", loginId);
    }

    // 로그인 ID가 추출되었는지 확인하고 없으면 예외 처리
    if (loginId == null) {
      throw new RuntimeException("Authorization token is missing or invalid");
    }

    return userRepository
        .findByLoginId(loginId)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
