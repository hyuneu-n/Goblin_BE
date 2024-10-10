package goblin.app.User.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import goblin.app.Common.exception.CustomValidationException;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupMember;
import goblin.app.Group.repository.GroupMemberRepository;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.User.model.dto.UserRegistrationResponseDTO;
import goblin.app.User.model.dto.UserSearchResponseDTO;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;
import goblin.app.User.util.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;

  // 회원가입 비지니스 로직
  public UserRegistrationResponseDTO registerUser(
      String loginId, String password, String username) {

    Map<String, String> errors = new HashMap<>(); // 오류를 담을 맵

    // 이름 한글 검증
    if (!username.matches("^[가-힣]+$")) {
      errors.put("username", "이름은 한글만 입력 가능합니다.");
    }

    // 아이디 길이 검증 (8자 이상)
    if (loginId.length() < 8) {
      errors.put("loginId", "아이디는 8자 이상이어야 합니다.");
    }

    // 비밀번호 길이 검증 (10자 이상)
    if (password.length() < 10) {
      errors.put("password", "비밀번호는 10자 이상이어야 합니다.");
    }

    // 오류가 있을 경우 예외 던지기
    if (!errors.isEmpty()) {
      throw new CustomValidationException(errors);
    }

    // 새로운 사용자 생성
    User user = new User();
    user.setLoginId(loginId);
    user.setLoginPw(passwordEncoder.encode(password));
    user.setUserRole("ROLE_USER");
    user.setUsername(username);

    // User 정보 DB에 저장
    // 새로운 사용자 저장
    User savedUser = userRepository.save(user);

    // "개인" 그룹이 이미 존재하는지 확인 후 없으면 생성
    Group personalGroup =
        groupRepository
            .findByGroupNameAndCreatedBy("개인", savedUser)
            .orElseGet(
                () -> {
                  Group newGroup = new Group();
                  newGroup.setGroupName("개인");
                  newGroup.setCreatedBy(savedUser);
                  return groupRepository.save(newGroup);
                });

    // 그룹과 사용자 연결 (GroupMember 생성)
    GroupMember groupMember = new GroupMember();
    groupMember.setUser(savedUser);
    groupMember.setGroupId(personalGroup.getGroupId());
    groupMember.setRole("USER");
    groupMemberRepository.save(groupMember);

    // 응답 DTO 생성 및 반환
    return new UserRegistrationResponseDTO(
        savedUser.getUsername(), savedUser.getLoginId(), "회원가입이 성공적으로 완료되었습니다.");
  }

  public boolean isLoginIdAvailable(String loginId) {
    return !userRepository.findByLoginId(loginId).isPresent();
  }

  public String loginUser(String loginId, String password) {
    // 1. 유저 정보 가져오기
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("Invalid login ID or password"));

    // 2. 비밀번호 확인
    if (!passwordEncoder.matches(password, user.getLoginPw())) {
      throw new RuntimeException("Invalid login ID or password");
    }

    // 3. "개인" 그룹 가져오기 - createdBy가 현재 로그인하는 유저인 그룹을 찾음
    Group personalGroup =
        groupRepository
            .findByGroupNameAndCreatedBy("개인", user)
            .orElseThrow(() -> new RuntimeException("개인 그룹을 찾을 수 없습니다."));

    // 4. JWT 토큰 생성 및 반환
    return jwtUtil.createAccessToken(user.getLoginId(), user.getUsername(), user.getUserRole());
  }

  // 회원 탈퇴 로직
  public void deleteUser(String loginId) {
    // 사용자 정보 가져오기
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("탈퇴할 사용자를 찾을 수 없습니다."));

    // 사용자 삭제
    userRepository.delete(user);
    log.info("회원 탈퇴 성공: 사용자 ID - {}", loginId);
  }

  public User saveRefreshToken(String loginId, String refreshToken) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    user.setRefreshToken(refreshToken);
    return userRepository.save(user);
  }

  public User findUserByLoginId(String loginId) {
    return userRepository
        .findByLoginId(loginId)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByLoginId(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority(user.getUserRole()));

    return new org.springframework.security.core.userdetails.User(
        user.getLoginId(), user.getLoginPw(), authorities);
  }

  public String getUserRoleByLoginId(String loginId) {
    User user = findUserByLoginId(loginId);
    return user.getUserRole(); // 역할 정보 반환
  }

  // 로그인 아이디로 사용자를 검색하는 메서드
  public List<UserSearchResponseDTO> searchUsersByLoginId(String loginId) {
    List<User> users = userRepository.findByLoginIdContaining(loginId);
    return users.stream()
        .map(user -> new UserSearchResponseDTO(user.getLoginId(), user.getUsername()))
        .collect(Collectors.toList());
  }
}
