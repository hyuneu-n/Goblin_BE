package goblin.app.Group.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import goblin.app.Group.model.dto.AvailableTimeRequestDTO;
import goblin.app.Group.model.dto.GroupCalendarRequestDTO;
import goblin.app.Group.model.dto.TimeRange;
import goblin.app.Group.model.dto.TimeSlot;
import goblin.app.Group.model.entity.AvailableTime;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;
import goblin.app.Group.model.entity.GroupCalendarParticipant;
import goblin.app.Group.model.entity.GroupMember;
import goblin.app.Group.repository.AvailableTimeRepository;
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
  private final AvailableTimeRepository availableTimeRepository;

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

  // 그룹 방장 여부 확인 로직
  public Group validateGroupOwner(Long groupId, String loginId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

    if (!group.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("해당 그룹의 관리자가 아닙니다.");
    }
    return group; // Group 객체를 반환
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

    // creatorLoginId를 이용해 User 객체 조회
    User creator =
        userRepository
            .findByLoginId(creatorLoginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + creatorLoginId));

    // 일정 생성 로직
    GroupCalendar groupCalendar = new GroupCalendar();
    groupCalendar.setGroupId(groupId);
    groupCalendar.setTitle(request.getTitle());
    groupCalendar.setStartDate(request.getStartDate()); // 시작 날짜
    groupCalendar.setEndDate(request.getEndDate()); // 종료 날짜

    // TimeRange 정보를 LocalTime으로 변환하여 설정
    LocalTime time = convertAmPmToLocalTime(request.getTimeRange());
    groupCalendar.setTime(time); // 예상 시간 설정

    groupCalendar.setPlace(request.getPlace());
    groupCalendar.setLink(request.getLink()); // 링크 설정
    groupCalendar.setNote(request.getNote()); // 메모 설정
    groupCalendar.setCreatedDate(LocalDateTime.now());
    groupCalendar.setCreatedBy(creator); // User 객체 설정

    groupCalendarRepository.save(groupCalendar);

    // 일정을 생성한 사용자도 자동으로 참여자로 추가
    GroupCalendarParticipant masterParticipant = new GroupCalendarParticipant();
    masterParticipant.setCalendarId(groupCalendar.getId());
    masterParticipant.setUserId(creator.getId());
    groupCalendarParticipantRepository.save(masterParticipant);

    // 요청에 있는 참여자 추가
    for (String loginId : request.getParticipants()) {
      User user =
          userRepository
              .findByLoginId(loginId)
              .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

      GroupCalendarParticipant participant = new GroupCalendarParticipant();
      participant.setCalendarId(groupCalendar.getId());
      participant.setUserId(user.getId());
      groupCalendarParticipantRepository.save(participant);
    }

    log.info("그룹 일정 등록 완료: 그룹ID = {}, 일정 제목 = {}", groupId, request.getTitle());
  }

  public boolean isUserInGroup(Long groupId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: loginId = " + loginId));

    return groupMemberRepository.findByGroupIdAndUser(groupId, user).isPresent();
  }

  // 그룹 삭제
  public void deleteGroup(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));
    groupRepository.delete(group);
    log.info("그룹이 삭제되었습니다: groupId = {}", groupId);
  }

  // 그룹 멤버 삭제
  public void removeMember(Long groupId, Long memberId) {
    GroupMember groupMember =
        groupMemberRepository
            .findById(memberId)
            .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다: memberId=" + memberId));

    if (!groupMember.getGroupId().equals(groupId)) {
      throw new RuntimeException("해당 그룹에 속하지 않은 멤버입니다.");
    }

    groupMemberRepository.delete(groupMember);
    log.info("그룹 멤버가 삭제되었습니다: memberId = {}, groupId = {}", memberId, groupId);
  }

  // 그룹명 수정
  public void updateGroupName(Long groupId, String groupName) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));
    group.setGroupName(groupName);
    groupRepository.save(group);
    log.info("그룹명이 수정되었습니다: groupId = {}, groupName = {}", groupId, groupName);
  }

  // 메모 추가
  public void addMemo(Long calendarId, String memo) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));
    calendar.setNote(memo);
    groupCalendarRepository.save(calendar);
    log.info("메모가 추가되었습니다: calendarId = {}, memo = {}", calendarId, memo);
  }

  // 그룹 조회
  public List<Group> getUserGroups(String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + loginId));

    List<Group> groups = new ArrayList<>();
    List<GroupMember> groupMembers = groupMemberRepository.findAllByUser(user);

    for (GroupMember groupMember : groupMembers) {
      Group group =
          groupRepository
              .findById(groupMember.getGroupId())
              .orElseThrow(
                  () ->
                      new RuntimeException("그룹을 찾을 수 없습니다: groupId = " + groupMember.getGroupId()));
      groups.add(group);
    }

    return groups;
  }

  // 일정 삭제
  public void deleteCalendarEvent(Long calendarId) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));
    calendar.setDeleted(true); // Soft Delete 플래그 설정
    groupCalendarRepository.save(calendar);
    log.info("일정이 삭제되었습니다 (Soft Delete): calendarId = {}", calendarId);
  }

  // 일정 확정
  public void confirmCalendarEvent(Long calendarId) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));
    calendar.setConfirmed(true); // 일정 확정
    groupCalendarRepository.save(calendar);
    log.info("그룹 일정이 확정되었습니다: calendarId = {}", calendarId);
  }

  // 그룹 캘린더 조회
  public List<GroupCalendar> getGroupCalendar(Long groupId) {
    // 그룹 캘린더 목록을 그룹 ID를 기준으로 조회
    return groupCalendarRepository.findAllByGroupId(groupId);
  }

  // 그룹 일정 수정 로직
  @Transactional
  public void updateGroupEvent(Long calendarId, GroupCalendarRequestDTO request, String loginId) {
    // 수정할 일정을 찾음
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId = " + calendarId));

    // 일정 수정 권한 확인 (예: 방장만 수정 가능)
    if (!calendar.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("일정을 수정할 권한이 없습니다.");
    }

    // 일정 정보 업데이트
    calendar.setTitle(request.getTitle());
    calendar.setPlace(request.getPlace());

    // 여러 날짜 중 첫 번째 날짜를 시작 날짜로 설정
    calendar.setStartDate(request.getStartDate());

    // 종료 날짜 설정
    calendar.setEndDate(request.getEndDate());

    // 시간 정보 업데이트
    LocalTime time = convertAmPmToLocalTime(request.getTimeRange());
    calendar.setTime(time); // LocalTime 설정

    groupCalendarRepository.save(calendar); // 수정된 일정 저장

    // 일정에 참가자 재설정 (기존 참여자를 모두 삭제하고 다시 추가)
    groupCalendarParticipantRepository.deleteAllByCalendarId(calendarId);

    for (String participantLoginId : request.getParticipants()) {
      User participant =
          userRepository
              .findByLoginId(participantLoginId)
              .orElseThrow(
                  () -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + participantLoginId));
      GroupCalendarParticipant calendarParticipant = new GroupCalendarParticipant();
      calendarParticipant.setCalendarId(calendarId);
      calendarParticipant.setUserId(participant.getId());
      groupCalendarParticipantRepository.save(calendarParticipant);
    }

    log.info("일정이 수정되었습니다: calendarId = {}", calendarId);
  }

  private LocalTime convertAmPmToLocalTime(TimeRange timeRange) {
    int hour = timeRange.getHour();
    String amPm = timeRange.getAmPm();

    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0; // AM 12시는 0시로 처리
    }

    return LocalTime.of(hour, timeRange.getMinute());
  }

  // 가능한 시간대 등록 로직
  public void setAvailableTime(Long calendarId, AvailableTimeRequestDTO request) {
    User user =
        userRepository
            .findByLoginId(request.getLoginId())
            .orElseThrow(
                () -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + request.getLoginId()));

    AvailableTime availableTime = new AvailableTime();
    availableTime.setUserId(user.getId());
    availableTime.setCalendarId(calendarId);
    availableTime.setStartTime(request.getStartTime());
    availableTime.setEndTime(request.getEndTime());

    availableTimeRepository.save(availableTime);
    log.info("참여자의 가능 시간 등록 완료: calendarId = {}, userId = {}", calendarId, user.getId());
  }

  // 참여자들의 시간대를 바탕으로 가장 많이 선택된 시간대 계산
  public List<TimeSlot> calculateOptimalTime(Long calendarId) {
    List<AvailableTime> availableTimes = availableTimeRepository.findByCalendarId(calendarId);

    // 시간대별로 카운팅하는 로직 (가장 많이 선택된 시간대 찾기)
    Map<TimeSlot, Integer> timeSlotCount = new HashMap<>();

    for (AvailableTime time : availableTimes) {
      TimeSlot slot = new TimeSlot(time.getStartTime(), time.getEndTime());
      timeSlotCount.put(slot, timeSlotCount.getOrDefault(slot, 0) + 1);
    }

    // 가장 많이 선택된 시간대들을 정렬하여 반환
    return timeSlotCount.entrySet().stream()
        .sorted(Map.Entry.<TimeSlot, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  // 일정 확정 로직 (참여자들이 선택한 시간 중에서 하나를 선택)
  public void confirmEventTime(Long calendarId, TimeSlot chosenSlot) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));

    calendar.setStartDate(chosenSlot.getStartTime());
    calendar.setEndDate(chosenSlot.getEndTime());
    calendar.setConfirmed(true);

    groupCalendarRepository.save(calendar);
    log.info("일정이 확정되었습니다: calendarId = {}", calendarId);
  }

  // 그룹명 수정 로직
  public void updateGroupName(Long groupId, String groupName, String loginId) {
    Group group = validateGroupOwner(groupId, loginId);
    group.setGroupName(groupName);
    groupRepository.save(group);
    log.info("그룹명이 수정되었습니다: groupId = {}, groupName = {}", groupId, groupName);
  }

  // 그룹 삭제 로직
  public void deleteGroup(Long groupId, String loginId) {
    Group group = validateGroupOwner(groupId, loginId);
    groupRepository.delete(group);
    log.info("그룹이 삭제되었습니다: groupId = {}", groupId);
  }

  // 그룹 멤버 삭제 로직
  public void removeMember(Long groupId, String memberLoginId, String loginId) {
    // 그룹 소유자 검증 (방장이 맞는지 확인)
    validateGroupOwner(groupId, loginId);

    // 그룹 ID와 멤버의 loginId를 기준으로 그룹 멤버를 찾음
    GroupMember groupMember =
        groupMemberRepository
            .findByGroupIdAndUser_LoginId(groupId, memberLoginId)
            .orElseThrow(() -> new RuntimeException("해당 그룹에 속하지 않은 멤버입니다."));

    // 멤버 삭제
    groupMemberRepository.delete(groupMember);
    log.info("그룹 멤버가 삭제되었습니다: loginId = {}, groupId = {}", memberLoginId, groupId);
  }
}
