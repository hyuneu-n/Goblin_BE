package goblin.app.Calendar.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goblin.app.Calendar.model.dto.request.uCalEditRequestDto;
import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.dto.response.uCalResponseDto;
import goblin.app.Calendar.service.UserCalService;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/calendar/user")
@RequiredArgsConstructor
@Slf4j
public class UserCalController {

  @Autowired private final UserCalService userCalService;
  @Autowired private final JwtUtil jwtUtil;

  @Autowired private final UserRepository userRepository;

  // 유저 캘린더 저장
  @PostMapping("/save")
  public ResponseEntity<uCalResponseDto> save(
      @RequestBody @Valid uCalSaveRequestDto requestDto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      uCalResponseDto responseDto = userCalService.save(requestDto, user);
      return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    } catch (RuntimeException e) {
      log.error("유저 캘린더 저장 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 유저 캘린더 수정
  @PutMapping("/edit")
  public ResponseEntity<uCalResponseDto> edit(
      @RequestBody @Valid uCalEditRequestDto requestDto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      uCalResponseDto responseDto = userCalService.edit(requestDto, user);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    } catch (RuntimeException e) {
      log.error("유저 캘린더 수정 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 유저 캘린더 삭제
  @DeleteMapping("/delete/{scheduleId}")
  public ResponseEntity<uCalResponseDto> delete(
      @PathVariable Long scheduleId,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      uCalResponseDto responseDto = userCalService.deleteById(scheduleId, user);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    } catch (RuntimeException e) {
      log.error("유저 캘린더 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 특정 달의 캘린더 조회
  @GetMapping("/view-month")
  public ResponseEntity<List<uCalResponseDto>> viewByMonth(
      @RequestParam int year,
      @RequestParam int month,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      List<uCalResponseDto> calList = userCalService.viewByMonth(year, month, user);
      return ResponseEntity.status(HttpStatus.OK).body(calList);
    } catch (RuntimeException e) {
      log.error("유저 캘린더 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 특정 일의 캘린더 조회
  @GetMapping("/view-day")
  public ResponseEntity<List<uCalResponseDto>> viewByDay(
      @RequestParam int year,
      @RequestParam int month,
      @RequestParam int day,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      List<uCalResponseDto> calList = userCalService.viewByDay(year, month, day, user);
      return ResponseEntity.status(HttpStatus.OK).body(calList);
    } catch (RuntimeException e) {
      log.error("유저 캘린더 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 카테고리별 스케줄 조회
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<uCalResponseDto>> viewByCategory(
      @PathVariable Long categoryId,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      List<uCalResponseDto> response = userCalService.viewByCategory(categoryId, user);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      log.error("카테고리별 스케줄 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // 고정 스케줄 검색
  @GetMapping("/search")
  public ResponseEntity<List<uCalResponseDto>> searchSchedules(
      @RequestParam String keyword,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      User user = getUserFromToken(bearerToken);
      List<uCalResponseDto> response = userCalService.searchSchedules(keyword, user);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      log.error("고정 스케줄 검색 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  // JWT 토큰에서 User 객체 추출하는 메서드
  private User getUserFromToken(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      String loginId = claims.getId();

      return userRepository
          .findByLoginId(loginId)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }
    throw new RuntimeException("Authorization token is missing or invalid");
  }
}
