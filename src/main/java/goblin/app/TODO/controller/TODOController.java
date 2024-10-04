package goblin.app.TODO.controller;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goblin.app.Group.service.GroupService;
import goblin.app.TODO.model.dto.TODORequestDTO;
import goblin.app.TODO.model.dto.TODOResponseDTO;
import goblin.app.TODO.service.TODOService;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/groups/{groupId}/todos")
@RequiredArgsConstructor
public class TODOController {

  private final TODOService todoService;
  private final GroupService groupService;
  private final JwtUtil jwtUtil;

  @Operation(summary = "TODO 생성", description = "새로운 TODO를 생성")
  @PostMapping
  public ResponseEntity<TODOResponseDTO> createTODO(
      @PathVariable Long groupId,
      @RequestBody TODORequestDTO request,
      @RequestHeader("Authorization") String token) {

    // 토큰에서 사용자 로그인 아이디 추출
    String loginId = extractLoginId(token);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    // TODO 생성
    TODOResponseDTO newTodo = todoService.createTODO(groupId, request, token);
    return ResponseEntity.ok(newTodo);
  }

  @Operation(summary = "TODO 완료", description = "TODO를 완료로 표시")
  @PutMapping("/{todoId}/complete")
  public ResponseEntity<String> markCompleted(
      @PathVariable Long groupId,
      @PathVariable Long todoId,
      @RequestHeader("Authorization") String token) {

    // 토큰에서 사용자 로그인 아이디 추출
    String loginId = extractLoginId(token);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // TODO 완료 처리
    todoService.markCompleted(todoId, token);
    return ResponseEntity.ok("TODO가 완료로 표시되었습니다.");
  }

  @Operation(summary = "미완료 TODO 목록 조회", description = "아직 완료되지 않은 TODO 목록을 조회")
  @GetMapping("/pending")
  public ResponseEntity<List<TODOResponseDTO>> getPendingTODOs(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    // 토큰에서 loginId 추출
    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인하는 로직
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Forbidden 응답
    }

    return ResponseEntity.ok(todoService.getPendingTODOs(groupId));
  }

  @Operation(summary = "완료된 TODO 목록 조회", description = "완료된 TODO 목록을 조회")
  @GetMapping("/completed")
  public ResponseEntity<List<TODOResponseDTO>> getCompletedTODOs(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    // 토큰에서 loginId 추출
    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인하는 로직
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Forbidden 응답
    }

    return ResponseEntity.ok(todoService.getCompletedTODOs(groupId));
  }

  @Operation(summary = "TODO 완료 취소", description = "완료된 TODO의 완료 상태를 취소")
  @PutMapping("/{todoId}/cancel-completion")
  public ResponseEntity<String> cancelCompleted(
      @PathVariable Long groupId,
      @PathVariable Long todoId,
      @RequestHeader("Authorization") String token) {

    // 그룹 멤버인지 확인하는 로직 필요
    if (!groupService.isUserInGroup(groupId, extractLoginId(token))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("그룹 멤버가 아닙니다.");
    }

    // TODO 완료 취소 로직
    todoService.cancelCompleted(todoId, token);

    return ResponseEntity.ok("TODO 완료 상태가 취소되었습니다.");
  }

  @Operation(summary = "TODO 수정", description = "TODO의 제목과 마감일을 수정")
  @PutMapping("/{todoId}")
  public ResponseEntity<TODOResponseDTO> updateTODO(
      @PathVariable Long groupId,
      @PathVariable Long todoId,
      @RequestBody TODORequestDTO request,
      @RequestHeader("Authorization") String token) {

    String loginId = extractLoginId(token);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    // 투두 수정
    TODOResponseDTO updatedTodo = todoService.updateTODO(todoId, request, loginId);
    return ResponseEntity.ok(updatedTodo);
  }

  @Operation(summary = "TODO 삭제", description = "TODO를 삭제")
  @DeleteMapping("/{todoId}")
  public ResponseEntity<String> deleteTODO(
      @PathVariable Long groupId,
      @PathVariable Long todoId,
      @RequestHeader("Authorization") String token) {

    String loginId = extractLoginId(token);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // 투두 삭제
    todoService.deleteTODO(todoId);
    return ResponseEntity.ok("TODO가 삭제되었습니다.");
  }

  @Operation(summary = "등록 날짜에 따른 TODO 목록 조회", description = "특정 날짜에 따라 TODO 목록을 조회")
  @GetMapping("/todos/date")
  public ResponseEntity<List<TODOResponseDTO>> getTODOsByDate(
      @PathVariable Long groupId,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestHeader("Authorization") String token) {

    List<TODOResponseDTO> todos = todoService.getTODOsByDate(groupId, date);
    return ResponseEntity.ok(todos);
  }

  private String extractLoginId(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      return claims.getId(); // 토큰에서 loginId 추출
    }
    return null;
  }
}
