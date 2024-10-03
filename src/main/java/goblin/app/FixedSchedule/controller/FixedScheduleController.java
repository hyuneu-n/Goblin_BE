package goblin.app.FixedSchedule.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goblin.app.FixedSchedule.model.dto.FixedScheduleRequestDTO;
import goblin.app.FixedSchedule.model.dto.FixedScheduleResponseDTO;
import goblin.app.FixedSchedule.service.FixedScheduleService;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/fixed")
@RequiredArgsConstructor
@Slf4j
public class FixedScheduleController {

  private final FixedScheduleService fixedScheduleService;
  private final JwtUtil jwtUtil;

  // 고정 일정 생성
  @Operation(summary = "고정 일정 생성", description = "사용자의 고정 일정을 등록")
  @PostMapping("/create")
  public ResponseEntity<?> createFixedSchedule(
      @RequestBody FixedScheduleRequestDTO dto,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      fixedScheduleService.createFixedSchedule(dto, loginId);
      return ResponseEntity.ok("고정 일정이 등록되었습니다.");
    } catch (RuntimeException e) {
      log.error("고정 일정 등록 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 사용자의 고정 일정 조회
  @Operation(summary = "사용자의 고정 일정 조회", description = "로그인한 사용자의 고정 일정 리스트를 조회")
  @GetMapping("/user")
  public ResponseEntity<?> getUserFixedSchedules(
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      List<FixedScheduleResponseDTO> schedules =
          fixedScheduleService.getUserFixedSchedules(loginId);
      return ResponseEntity.ok(schedules);
    } catch (RuntimeException e) {
      log.error("고정 일정 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 고정 일정 시간 및 요일 업데이트
  @Operation(summary = "고정 일정 시간 및 요일 업데이트", description = "기존 고정 일정의 요일, 시작 및 종료 시간을 업데이트")
  @PutMapping("/{scheduleId}/update")
  public ResponseEntity<?> updateFixedSchedule(
      @PathVariable Long scheduleId,
      @RequestBody FixedScheduleRequestDTO updateRequest,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      fixedScheduleService.updateFixedSchedule(scheduleId, updateRequest, loginId);
      return ResponseEntity.ok("고정 일정이 업데이트되었습니다.");
    } catch (RuntimeException e) {
      log.error("고정 일정 업데이트 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // JWT 토큰에서 loginId 추출
  private String extractLoginId(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      return claims.getId(); // 토큰에서 loginId 추출
    }
    return null;
  }

  // 고정 일정 삭제
  @Operation(summary = "고정 일정 삭제", description = "기존 고정 일정을 삭제합니다.")
  @DeleteMapping("/delete/{scheduleId}")
  public ResponseEntity<?> deleteFixedSchedule(
      @PathVariable Long scheduleId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      fixedScheduleService.deleteFixedSchedule(scheduleId, loginId);
      return ResponseEntity.ok("고정 일정이 삭제되었습니다.");
    } catch (RuntimeException e) {
      log.error("고정 일정 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
