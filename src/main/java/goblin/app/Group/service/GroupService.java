package goblin.app.Group.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import goblin.app.Group.model.dto.GroupCalendarRequestDTO;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;
import goblin.app.Group.model.entity.GroupCalendarParticipant;
import goblin.app.Group.model.entity.GroupMember;
import goblin.app.Group.repository.GroupCalendarParticipantRepository;
import goblin.app.Group.repository.GroupCalendarRepository;
import goblin.app.Group.repository.GroupMemberRepository;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final GroupCalendarRepository groupCalendarRepository;
  private final GroupCalendarParticipantRepository groupCalendarParticipantRepository;
  private final UserRepository userRepository;

  // 그룹 생성 로직
  public void createGroup(String groupName, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    // 그룹 생성
    Group group = new Group();
    group.setGroupName(groupName);
    group.setCreatedBy(user); // User 객체 설정
    groupRepository.save(group);

    // 그룹장 자동 설정
    GroupMember groupMember = new GroupMember();
    groupMember.setUser(user);
    groupMember.setGroupId(group.getGroupId());
    groupMember.setRole("MASTER");
    groupMemberRepository.save(groupMember);

    log.info("그룹 생성 완료: 그룹명 - {}, 그룹장 - {}", groupName, loginId);
  }

  // 그룹 주인 검증 로직
  public void validateGroupOwner(Long groupId, String loginId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

    if (!group.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("해당 그룹의 관리자가 아닙니다.");
    }

    // 그룹 멤버 중에서 MASTER가 있는지 확인
    GroupMember groupMember =
        groupMemberRepository
            .findByGroupIdAndUser(groupId, group.getCreatedBy())
            .orElseThrow(() -> new RuntimeException("그룹의 관리자를 찾을 수 없습니다."));

    if (!"MASTER".equals(groupMember.getRole())) {
      throw new RuntimeException("그룹의 초대 권한이 없습니다.");
    }

    log.info("그룹의 관리자가 확인되었습니다: groupId={}, loginId={}", groupId, loginId);
  }

  // 그룹 멤버 초대 로직
  public void inviteMember(Long groupId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("해당 로그인 ID를 가진 사용자를 찾을 수 없습니다: " + loginId));

    // 그룹 멤버로 추가
    GroupMember groupMember = new GroupMember();
    groupMember.setUser(user);
    groupMember.setGroupId(groupId);
    groupMember.setRole("MEMBER");
    groupMemberRepository.save(groupMember);

    log.info("멤버 초대 완료: 그룹ID - {}, 초대된 사용자 - {}", groupId, loginId);
  }

  // 그룹 일정 생성 로직
  public void createGroupEvent(
      Long groupId, GroupCalendarRequestDTO request, String creatorLoginId) {

    // 여러 날짜를 처리하는 로직
    for (LocalDate date : request.getDates()) {
      GroupCalendar groupCalendar = new GroupCalendar();
      groupCalendar.setGroupId(groupId);
      groupCalendar.setTitle(request.getTitle());
      groupCalendar.setDate(date);

      // 시간 범위를 AM/PM 정보를 포함하여 처리
      int hour = request.getTimeRange().getHour();
      int minute = request.getTimeRange().getMinute();
      String amPm = request.getTimeRange().getAmPm();

      // AM/PM 정보를 처리하여 24시간 기준으로 변환
      if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
        hour += 12;
      } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
        hour = 0; // AM 12시는 0시로 처리
      }

      LocalTime time = LocalTime.of(hour, minute);
      groupCalendar.setTime(time); // LocalTime을 설정

      log.info("그룹 일정 등록 중: 그룹ID = {}, 날짜 = {}, 시간 = {}", groupId, date, time); // 로그 추가

      groupCalendar.setPlace(request.getPlace());
      groupCalendar.setCreatedDate(LocalDateTime.now());

      // 일정 정보 저장
      groupCalendarRepository.save(groupCalendar);

      // 참여자 저장 로직
      // 1. 일정을 생성한 master도 참가자로 추가
      User creator =
          userRepository
              .findByLoginId(creatorLoginId)
              .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + creatorLoginId));

      GroupCalendarParticipant masterParticipant = new GroupCalendarParticipant();
      masterParticipant.setCalendarId(groupCalendar.getId());
      masterParticipant.setUserId(creator.getId()); // 일정을 생성한 사용자를 저장
      groupCalendarParticipantRepository.save(masterParticipant);

      // 2. 요청에 있는 참여자들 추가
      for (String loginId : request.getParticipants()) {
        User user =
            userRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));
        GroupMember participant =
            groupMemberRepository
                .findByGroupIdAndUser(groupId, user)
                .orElseThrow(() -> new RuntimeException("참여자를 찾을 수 없습니다: loginId=" + loginId));

        GroupCalendarParticipant groupCalendarParticipant = new GroupCalendarParticipant();
        groupCalendarParticipant.setCalendarId(groupCalendar.getId());
        groupCalendarParticipant.setUserId(user.getId()); // 유저 ID를 저장
        groupCalendarParticipantRepository.save(groupCalendarParticipant);
      }
    }

    log.info("그룹 일정 등록 및 참여자 저장 완료: 그룹ID - {}, 일정 제목 - {}", groupId, request.getTitle());
  }

  public boolean isUserInGroup(Long groupId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: loginId = " + loginId));

    return groupMemberRepository.findByGroupIdAndUser(groupId, user).isPresent();
  }
}
