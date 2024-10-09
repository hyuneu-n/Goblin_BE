package goblin.app.Group.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import goblin.app.Group.model.dto.*;
import goblin.app.Group.service.GroupService;
import goblin.app.Group.service.InviteTokenService;
import goblin.app.Notification.model.entity.EmitterRepository;
import goblin.app.Notification.service.NotificationService;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "그룹")
public class GroupController {

  private final GroupService groupService;
  private final JwtUtil jwtUtil;
  private final InviteTokenService inviteTokenService;

  private final NotificationService notificationService;

  private final UserRepository userRepository;

  private final EmitterRepository emitterRepository;

  // 그룹 생성
  @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성하고 그룹장을 자동으로 설정")
  @PostMapping
  public ResponseEntity<?> createGroup(
      @RequestBody GroupRequestDTO request,
      @RequestHeader(value = "Authorization", required = false) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.createGroup(request.getGroupName(), loginId);
      return ResponseEntity.ok("그룹 생성이 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 생성 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 그룹 멤버 초대
  @Operation(summary = "그룹 멤버 초대", description = "그룹에 멤버 초대 (그룹장만 가능)")
  @PostMapping("/{groupId}/invite")
  public ResponseEntity<?> inviteMember(
      @PathVariable Long groupId,
      @RequestBody GroupMemberRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.validateGroupOwner(groupId, loginId);
      groupService.inviteMember(groupId, request.getLoginId());
      return ResponseEntity.ok("멤버 초대가 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("멤버 초대 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 그룹 일정 등록
  @Operation(summary = "그룹 일정 등록", description = "그룹 일정을 등록")
  @PostMapping("/{groupId}/calendar")
  public ResponseEntity<?> createGroupEvent(
      @PathVariable Long groupId,
      @RequestBody GroupCalendarRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.createGroupEvent(groupId, request, loginId);
      return ResponseEntity.ok("그룹 일정 등록이 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 일정 등록 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 일정 삭제 (Soft Delete)
  @Operation(summary = "그룹 일정 삭제", description = "그룹 일정을 삭제 (주최자만 가능)")
  @DeleteMapping("/{groupId}/calendar/{calendarId}")
  public ResponseEntity<?> deleteCalendarEvent(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.deleteCalendarEvent(calendarId, loginId);
      return ResponseEntity.ok("일정이 삭제되었습니다.");
    } catch (RuntimeException e) {
      log.error("일정 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 일정 수정
  @Operation(summary = "그룹 일정 수정", description = "그룹 일정을 수정")
  @PutMapping("/calendar/{calendarId}")
  public ResponseEntity<?> updateGroupEvent(
      @PathVariable Long calendarId,
      @RequestBody GroupCalendarRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.updateGroupEvent(calendarId, request, loginId);
      return ResponseEntity.ok("일정 수정이 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("일정 수정 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 메모 추가
  @Operation(summary = "메모 추가", description = "일정에 메모를 추가")
  @PostMapping("/{groupId}/calendar/{calendarId}/memo")
  public ResponseEntity<?> addMemoToCalendar(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestBody String memo,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.addMemo(calendarId, memo);
      return ResponseEntity.ok("메모가 추가되었습니다.");
    } catch (RuntimeException e) {
      log.error("메모 추가 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "확정된 일정 조회", description = "확정된 일정을 조회")
  @GetMapping("/{groupId}/calendar/{calendarId}/confirmed")
  public ResponseEntity<?> getConfirmedCalendar(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // 서비스에서 확정된 일정을 가져와서 DTO로 반환
    GroupConfirmedCalendarDTO confirmedCalendar =
        groupService.getConfirmedCalendar(groupId, calendarId);
    return ResponseEntity.ok(confirmedCalendar);
  }

  // 새로운 일정 확정 로직 (범위 내에서 시간 선택)
  @Operation(
      summary = "일정 최종 확정",
      description = "가장 적합한 시간들을 계산한 후 그 리스트들 중에서 하나를 선택하고, 그 범위 내에서 시간을 완전히 확정")
  @PostMapping("/{groupId}/calendar/{calendarId}/confirm")
  public ResponseEntity<?> confirmCustomTimeInRange(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestBody ConfirmTimeRangeRequest request, // 요청으로 범위 내 시간 입력 받음
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);
    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // 범위 내에서 사용자 지정 시간 확정
    try {
      groupService.confirmCustomTimeInRange(
          calendarId, request.getOptimalTimeSlotId(), request, loginId);
      return ResponseEntity.ok("일정이 확정되었습니다.");
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 그룹 조회
  @Operation(summary = "그룹 조회", description = "로그인한 사용자의 그룹 목록을 조회")
  @GetMapping
  public ResponseEntity<?> getUserGroups(
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      List<GroupResponseDto> groups = groupService.getUserGroups(loginId);
      return ResponseEntity.ok(groups);
    } catch (RuntimeException e) {
      log.error("그룹 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 후보 일정 조회
  @Operation(
      summary = "그룹 후보 일정 조회 (확정 일정 X)",
      description =
          "팀원들한테 '며칠 몇시부터 며칠 몇시까지의 시간 중 가능한 시간 선택하셈' 을 보낼 때에서 '며칠 몇시부터 며칠 몇시'까지의 일정조회를 담당하는 api")
  @GetMapping("/{groupId}/calendar")
  public ResponseEntity<?> getGroupCalendar(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      // 토큰에서 loginId 추출
      String loginId = extractLoginId(bearerToken);

      // 사용자가 그룹에 속해 있는지 확인
      if (!groupService.isUserInGroup(groupId, loginId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
      }

      // 그룹 일정 조회
      List<GroupCalendarResponseDTO> events = groupService.getGroupCalendar(groupId);
      return ResponseEntity.ok(events);
    } catch (RuntimeException e) {
      log.error("그룹 캘린더 조회 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  private String extractLoginId(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      return claims.getId(); // 토큰에서 loginId 추출
    }
    return null;
  }

  // 그룹명 수정
  @Operation(summary = "그룹명 수정", description = "그룹명 수정 (방장만 가능)")
  @PutMapping("/{groupId}")
  public ResponseEntity<?> updateGroupName(
      @PathVariable Long groupId,
      @RequestBody GroupRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.updateGroupName(groupId, request.getGroupName(), loginId);
      return ResponseEntity.ok("그룹명이 수정되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹명 수정 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 그룹 삭제
  @Operation(summary = "그룹 삭제", description = "그룹 삭제(방장만 가능)")
  @DeleteMapping("/{groupId}")
  public ResponseEntity<?> deleteGroup(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.deleteGroup(groupId, loginId);
      return ResponseEntity.ok("그룹이 삭제되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 그룹 멤버 삭제
  @Operation(summary = "그룹 멤버 삭제", description = "그룹에서 멤버를 삭제합(방장만 가능)")
  @DeleteMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<?> removeMember(
      @PathVariable Long groupId,
      @PathVariable String memberId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {
    try {
      String loginId = extractLoginId(bearerToken);
      groupService.removeMember(groupId, memberId, loginId);
      return ResponseEntity.ok("그룹 멤버가 삭제되었습니다.");
    } catch (RuntimeException e) {
      log.error("그룹 멤버 삭제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "가능한 시간 제출", description = "참여자가 가능한 시간을 제출")
  @PostMapping("/{groupId}/calendar/{calendarId}/available-time")
  public ResponseEntity<?> submitAvailableTime(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestBody AvailableTimeRequestDTO request,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    groupService.setAvailableTime(calendarId, request, loginId);

    return ResponseEntity.ok("가능한 시간이 제출되었습니다.");
  }

  @Operation(summary = "최적 시간 계산", description = "참여자들이 제출한 시간을 기반으로 가장 많은 팀원이 가능한 시간을 계산")
  @GetMapping("/calendar/{groupId}/{calendarId}/optimal-time")
  public ResponseEntity<?> calculateOptimalTime(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    // 토큰에서 loginId 추출
    String loginId = extractLoginId(bearerToken);

    // 사용자가 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    try {
      // 최적 시간 계산 및 저장 로직 수행, TimeSlot 리스트 반환
      List<TimeSlot> timeSlots = groupService.calculateOptimalTimesAndSave(calendarId);

      // 변환된 TimeSlot 리스트를 성공적으로 반환
      return ResponseEntity.ok(timeSlots);
    } catch (RuntimeException e) {
      // 에러 처리
      log.error("최적 시간 계산 중 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("최적 시간 계산 중 오류가 발생했습니다.");
    }
  }

  @Operation(summary = "그룹 멤버 리스트 조회", description = "해당 그룹의 멤버 리스트를 반환합니다.")
  @GetMapping("/{groupId}/members")
  public ResponseEntity<?> getGroupMembers(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 사용자가 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // 그룹 멤버 리스트 가져오기
    List<GroupMemberResponseDTO> members = groupService.getGroupMembersWithRoles(groupId);
    return ResponseEntity.ok(members);
  }

  @Operation(summary = "초대 링크 생성", description = "그룹에 초대하는 초대 링크를 생성")
  @PostMapping("/{groupId}/invite-link")
  public ResponseEntity<?> generateInviteLink(
      @PathVariable Long groupId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 그룹장인지 확인
    groupService.validateGroupOwner(groupId, loginId);

    // 초대 링크 토큰 생성
    String inviteToken = inviteTokenService.generateInviteToken(groupId);

    // 초대 링크 반환
    String inviteLink = "http://localhost:8080//invite?token=" + inviteToken;
    return ResponseEntity.ok(inviteLink);
  }

  @Operation(summary = "초대 링크 처리", description = "초대 링크를 통해 그룹에 가입")
  @PostMapping("/join-by-invite")
  public ResponseEntity<?> joinGroupByInvite(
      @RequestParam String token,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 토큰 유효성 확인 및 그룹 ID 추출
    Long groupId = inviteTokenService.validateInviteToken(token);

    // 그룹에 멤버 추가
    groupService.inviteMember(groupId, loginId);

    return ResponseEntity.ok("그룹에 성공적으로 가입되었습니다.");
  }

  @Operation(summary = "그룹 일정 참가자 조회", description = "그룹의 일정에 참가한 사람들의 제출 여부 조회")
  @GetMapping("/{groupId}/calendar/{calendarId}/participants")
  public ResponseEntity<?> getParticipantsAvailability(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 그룹의 멤버가 아닙니다.");
    }

    // 서비스 호출해서 참가자 정보 가져오기
    List<GroupParticipantResponseDTO> participants =
        groupService.getParticipantsForCalendar(groupId, calendarId);

    return ResponseEntity.ok(participants);
  }

  @Operation(summary = "참가자의 가능한 시간 조회", description = "참가자가 제출한 가능한 시간을 조회")
  @GetMapping("/{groupId}/calendar/{calendarId}/available-times")
  public ResponseEntity<List<TimeSlot>> getAvailableTimes(
      @PathVariable Long groupId,
      @PathVariable Long calendarId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    String loginId = extractLoginId(bearerToken);

    // 그룹에 속해 있는지 확인
    if (!groupService.isUserInGroup(groupId, loginId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    List<TimeSlot> availableTimes = groupService.getAvailableTimesForCalendar(calendarId);
    return ResponseEntity.ok(availableTimes);
  }

  @Operation(summary = "알림 SSE 요청", description = "알림을 받는 메서드")
  @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> eventNotify(
      @RequestHeader(value = "Authorization", required = true) String bearerToken,
      HttpServletResponse response) {

    String loginId = extractLoginId(bearerToken);
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

    // 기존 Emitter가 있을 경우 제거
    SseEmitter existingEmitter = emitterRepository.get(user.getId());
    if (existingEmitter != null) {
      existingEmitter.complete();
      emitterRepository.deleteById(user.getId());
    }

    // X-Accel-Buffering 헤더 추가
    response.setHeader("X-Accel-Buffering", "no");

    // 새로운 SSE Emitter 반환
    SseEmitter emitter = notificationService.eventN(user.getId());
    return ResponseEntity.ok(emitter);
  }
}
