package goblin.app.User.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goblin.app.Common.exception.CustomValidationException;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.service.GroupService;
import goblin.app.User.model.dto.AuthResponse;
import goblin.app.User.model.dto.RefreshTokenRequest;
import goblin.app.User.model.dto.UserLoginRequest;
import goblin.app.User.model.dto.UserRegistrationRequest;
import goblin.app.User.model.dto.UserRegistrationResponseDTO;
import goblin.app.User.model.dto.UserSearchResponseDTO;
import goblin.app.User.model.entity.User;
import goblin.app.User.service.UserService;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "로그인/회원가입")
public class UserController {
  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final GroupService groupService;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록( 회원가입 )")
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
    try {
      // 회원가입 서비스 호출 (User Service 호출)
      UserRegistrationResponseDTO response =
          userService.registerUser(
              request.getLoginId(), request.getPassword(), request.getUsername());
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      // 예외 발생 시 로그 출력 디버깅용
      log.error("Error during user registration in controller /api/register: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "회원가입 중복된 아이디 확인", description = "회원가입 중복된 아이디 확인")
  @GetMapping("/check-id")
  public ResponseEntity<?> checkRegisterLoginId(@RequestParam String loginId) {
    try {
      boolean isAvailable = userService.isLoginIdAvailable(loginId);

      if (isAvailable) {
        return ResponseEntity.ok(Collections.singletonMap("available", true));
      } else {
        log.warn("회원가입 실패: 중복된 Login ID - {}", loginId);
        return ResponseEntity.status(HttpServletResponse.SC_CONFLICT)
            .body(Collections.singletonMap("available", false));
      }
    } catch (RuntimeException e) {
      log.error("Error during ID check in controller /api/check-id: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "로그인", description = "사용자가 로그인하고 토큰을 발급받음.")
  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
    try {
      // 로그인 서비스 호출
      String accessToken = userService.loginUser(request.getLoginId(), request.getPassword());

      // Refresh Token 생성 및 저장
      String userRole = userService.getUserRoleByLoginId(request.getLoginId());
      String refreshToken = jwtUtil.createRefreshToken(request.getLoginId(), userRole);
      userService.saveRefreshToken(request.getLoginId(), refreshToken);

      // 사용자 정보 가져오기 (닉네임 등)
      User user = userService.findUserByLoginId(request.getLoginId());

      // "개인" 그룹의 그룹 ID 가져오기
      Group personalGroup = groupService.getOrCreatePersonalGroup(user);
      Long personalGroupId = personalGroup.getGroupId();

      // 닉네임, 토큰, 개인 그룹 ID 등 응답에 포함
      AuthResponse authResponse =
          new AuthResponse(
              accessToken, refreshToken, user.getLoginId(), user.getUsername(), personalGroupId);
      return ResponseEntity.ok(authResponse);

    } catch (RuntimeException e) {
      log.error("Error during user login: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "토큰 재발급", description = "refresh token을 검증 후, 새로운 accessToken 발급.")
  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshAuthToken(@RequestBody RefreshTokenRequest request) {
    try {
      // Refresh Token 유효성 검증
      String refreshToken = request.getRefreshToken();
      if (!jwtUtil.validateToken(refreshToken, request.getLoginId())) {
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
            .body("Invalid Refresh Token");
      }
      // 새로운 Access Token 생성
      String newAccessToken =
          jwtUtil.createAccessToken(
              request.getLoginId(), request.getUsername(), request.getUserRole());
      return ResponseEntity.ok(new AuthResponse(newAccessToken));
    } catch (RuntimeException e) {
      // 예외 발생 시 로그 출력
      log.error("Error during token refresh in controller /api/refresh-token: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @Operation(summary = "회원 탈퇴", description = "사용자가 자신의 계정을 삭제")
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String bearerToken) {
    try {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      String loginId = claims.getId();

      // 회원 탈퇴 서비스 호출
      userService.deleteUser(loginId);

      log.info("회원 탈퇴 완료: 사용자 ID - {}", loginId);
      return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    } catch (RuntimeException e) {
      log.error("회원 탈퇴 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @RestControllerAdvice
  public class GlobalExceptionHandler {

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
        CustomValidationException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }
  }

  // 로그인 아이디로 서비스에 가입된 모든 사용자 검색
  @Operation(summary = "로그인 아이디로 유저 검색", description = "로그인 아이디로 유저 검색")
  @GetMapping("/search")
  public ResponseEntity<List<UserSearchResponseDTO>> searchUsersByLoginId(
      @RequestParam String loginId,
      @RequestHeader(value = "Authorization", required = true) String bearerToken) {

    // 헤더에서 토큰에서 loginId 추출
    String requesterLoginId = extractLoginId(bearerToken);

    // 추출된 로그인 아이디로 인증된 사용자인지 확인할 수 있는 로직을 추가
    if (requesterLoginId == null) {
      return ResponseEntity.status(403).body(null);
    }

    // 로그인 아이디로 사용자를 검색
    List<UserSearchResponseDTO> users = userService.searchUsersByLoginId(loginId);
    return ResponseEntity.ok(users);
  }
  // JWT 토큰에서 loginId 추출하는 메서드
  private String extractLoginId(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      return claims.getId(); // 토큰에서 loginId 추출
    }
    return null;
  }
}
