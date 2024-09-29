package goblin.app.Group.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goblin.app.Group.model.dto.GroupCalendarRequestDTO;
import goblin.app.Group.model.dto.GroupMemberRequestDTO;
import goblin.app.Group.model.dto.GroupRequestDTO;
import goblin.app.Group.service.GroupService;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

  private final GroupService groupService;
  private final JwtUtil jwtUtil;

  @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성하고 그룹장을 자동으로 설정")
  @PostMapping
  public ResponseEntity<?> createGroup(
      @RequestBody GroupRequestDTO request,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      String loginId = null;
      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        try {
          String token = bearerToken.substring(7);
          Claims claims = jwtUtil.getAllClaimsFromToken(token);
          loginId = claims.getId();
        } catch (Exception e) {
          log.error("Invalid token: {}", e.getMessage());
        }
      }
      // 그룹 생성
      groupService.createGroup(request.getGroupName(), loginId);

      return ResponseEntity.ok("그룹 생성이 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 생성 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "그룹 멤버 초대", description = "그룹에 멤버 초대 (그룹장만 가능)")
  @PostMapping("/{groupId}/invite")
  public ResponseEntity<?> inviteMember(
      @PathVariable Long groupId,
      @RequestBody GroupMemberRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = null;
      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        String token = bearerToken.substring(7);
        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        loginId = claims.getId();
      }
      // 방 주인이 맞는지 검증
      groupService.validateGroupOwner(groupId, loginId);
      // 초대 로직
      groupService.inviteMember(groupId, request.getLoginId());

      return ResponseEntity.ok("멤버 초대가 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("멤버 초대 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "그룹 일정 등록", description = "그룹 일정을 등록합니다.")
  @PostMapping("/{groupId}/calendar")
  public ResponseEntity<?> createGroupEvent(
      @PathVariable Long groupId,
      @RequestBody GroupCalendarRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = null;
      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        String token = bearerToken.substring(7);
        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        loginId = claims.getId(); // 토큰에서 loginId 추출
      }

      log.info("추출된 로그인 ID: {}", loginId);

      // 로그인한 사용자가 그룹에 속해있는지 확인
      if (!groupService.isUserInGroup(groupId, loginId)) {
        log.error("그룹에 속하지 않은 사용자: loginId = {}", loginId);
        return ResponseEntity.status(403).body("해당 그룹에 속하지 않은 사용자입니다.");
      }

      // 그룹 일정 생성
      groupService.createGroupEvent(groupId, request, loginId); // loginId를 함께 전달

      return ResponseEntity.ok("그룹 일정 등록이 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 일정 등록 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
