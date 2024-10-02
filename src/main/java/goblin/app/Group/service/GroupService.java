package goblin.app.Group.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import goblin.app.Calendar.model.dto.request.uCalSaveRequestDto;
import goblin.app.Calendar.model.entity.UserCalendar;
import goblin.app.Calendar.repository.UserCalendarRepository;
import goblin.app.Calendar.service.UserCalService;
import goblin.app.Category.model.entity.Category;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Group.model.dto.AvailableTimeRequestDTO;
import goblin.app.Group.model.dto.AvailableTimeSlot;
import goblin.app.Group.model.dto.GroupCalendarRequestDTO;
import goblin.app.Group.model.dto.GroupConfirmedCalendarDTO;
import goblin.app.Group.model.dto.GroupMemberResponseDTO;
import goblin.app.Group.model.dto.TimeRange;
import goblin.app.Group.model.dto.TimeSlot;
import goblin.app.Group.model.entity.AvailableTime;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;
import goblin.app.Group.model.entity.GroupCalendarParticipant;
import goblin.app.Group.model.entity.GroupConfirmedCalendar;
import goblin.app.Group.model.entity.GroupMember;
import goblin.app.Group.model.entity.OptimalTimeSlot;
import goblin.app.Group.repository.AvailableTimeRepository;
import goblin.app.Group.repository.GroupCalendarParticipantRepository;
import goblin.app.Group.repository.GroupCalendarRepository;
import goblin.app.Group.repository.GroupConfirmedCalendarRepository;
import goblin.app.Group.repository.GroupMemberRepository;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.Group.repository.OptimalTimeSlotRepository;
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
  private final GroupConfirmedCalendarRepository groupConfirmedCalendarRepository;
  private final UserCalService userCalService; // 개인 캘린더 서비스
  private final OptimalTimeSlotRepository optimalTimeSlotRepository;
  private final CategoryRepository categoryRepository;
  private final UserCalendarRepository userCalendarRepository;

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
            .findByIdAndNotDeleted(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

    if (!group.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("해당 그룹의 관리자가 아닙니다.");
    }
    return group;
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

  // 그룹 일정 등록 로직
  public void createGroupEvent(
      Long groupId, GroupCalendarRequestDTO request, String creatorLoginId) {
    User creator =
        userRepository
            .findByLoginId(creatorLoginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + creatorLoginId));

    if (!isUserInGroup(groupId, creatorLoginId)) {
      throw new RuntimeException("방장이 아닌 사용자는 일정을 생성할 수 없습니다.");
    }

    // 하나의 시간 범위로 모든 날짜에 대해 적용
    LocalTime startTime =
        convertToLocalTime(
            request.getTimeRange().getStartAmPm(),
            request.getTimeRange().getStartHour(),
            request.getTimeRange().getStartMinute());
    LocalTime endTime =
        convertToLocalTime(
            request.getTimeRange().getEndAmPm(),
            request.getTimeRange().getEndHour(),
            request.getTimeRange().getEndMinute());

    GroupCalendar groupCalendar = new GroupCalendar();
    groupCalendar.setGroupId(groupId);
    groupCalendar.setTitle(request.getTitle());
    groupCalendar.setSelectedDates(request.getDates()); // 선택된 날짜들 설정
    groupCalendar.setTime(request.getDuration()); // 예상 소요 시간 설정
    groupCalendar.setStartTime(startTime); // 시간 범위 설정
    groupCalendar.setEndTime(endTime);
    groupCalendar.setPlace(request.getPlace());
    groupCalendar.setLink(request.getLink());
    groupCalendar.setNote(request.getNote());
    groupCalendar.setCreatedDate(LocalDateTime.now());
    groupCalendar.setCreatedBy(creator);

    groupCalendarRepository.save(groupCalendar);

    // 참여자 등록
    for (String loginId : request.getParticipants()) {
      User user =
          userRepository
              .findByLoginId(loginId)
              .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

      if (!isUserInGroup(groupId, loginId)) {
        throw new RuntimeException("그룹 멤버가 아닌 사용자는 참여할 수 없습니다.");
      }

      GroupCalendarParticipant participant = new GroupCalendarParticipant();
      participant.setCalendarId(groupCalendar.getId());
      participant.setUserId(user.getId());
      groupCalendarParticipantRepository.save(participant);
    }

    log.info("그룹 일정 등록 완료: 그룹ID = {}, 일정 제목 = {}", groupId, request.getTitle());
  }

  // 로컬타임으로 전환
  private LocalTime convertToLocalTime(String amPm, int hour, int minute) {
    if ("PM".equalsIgnoreCase(amPm) && hour < 12) {
      hour += 12;
    } else if ("AM".equalsIgnoreCase(amPm) && hour == 12) {
      hour = 0; // AM 12시는 0시로 처리
    }

    return LocalTime.of(hour, minute);
  }

  // 사용자가 그룹에 있는지 검증
  public boolean isUserInGroup(Long groupId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: loginId = " + loginId));

    return groupMemberRepository.findByGroupIdAndUser(groupId, user).isPresent();
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

    // 유저가 속한 그룹(방장, 멤버 포함) 조회 + 삭제된 그룹은 조회 안됨
    return groupRepository.findAllByUserAsMember(user);
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

  // 그룹 캘린더 조회
  @Transactional
  public List<GroupCalendar> getGroupCalendar(Long groupId) {
    // 삭제되지 않은 그룹 캘린더 목록을 그룹 ID를 기준으로 조회
    return groupCalendarRepository.findAllByGroupIdAndNotDeleted(groupId);
  }

  // 그룹 일정 수정 로직
  @Transactional
  public void updateGroupEvent(Long calendarId, GroupCalendarRequestDTO request, String loginId) {
    // 수정할 일정을 찾음
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId = " + calendarId));

    // 일정 수정 권한 확인 (방장만 수정 가능)
    if (!calendar.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("일정을 수정할 권한이 없습니다.");
    }

    // 일정 정보 업데이트
    calendar.setTitle(request.getTitle());
    calendar.setPlace(request.getPlace());
    calendar.setSelectedDates(request.getDates());
    calendar.setNote(request.getNote());
    calendar.setLink(request.getLink());

    // 시간 정보 업데이트
    TimeRange timeRange = request.getTimeRange();
    LocalTime startTime =
        convertToLocalTime(
            timeRange.getStartAmPm(), timeRange.getStartHour(), timeRange.getStartMinute());
    LocalTime endTime =
        convertToLocalTime(
            timeRange.getEndAmPm(), timeRange.getEndHour(), timeRange.getEndMinute());
    calendar.setStartTime(startTime); // 시작 시간 설정
    calendar.setEndTime(endTime); // 종료 시간 설정

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

  // 가능한 시간대 등록 로직
  public void setAvailableTime(Long calendarId, AvailableTimeRequestDTO request, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + loginId));

    for (AvailableTimeSlot slot : request.getAvailableTimeSlots()) {
      LocalTime startTime = LocalTime.parse(slot.getStartTime());
      LocalTime endTime = LocalTime.parse(slot.getEndTime());
      LocalDateTime startDateTime = LocalDateTime.of(slot.getDate(), startTime);
      LocalDateTime endDateTime = LocalDateTime.of(slot.getDate(), endTime);

      // 고정 일정과 겹치는지 확인 후 저장
      uCalSaveRequestDto saveRequest =
          uCalSaveRequestDto
              .builder()
              .title("Available Time") // 저장 시 기본 제목 설정
              .startTime(startDateTime)
              .endTime(endDateTime)
              .build();

      userCalService.submitAvailableTime(saveRequest, user); // 가능한 시간 저장
      log.info("참여자의 가능 시간 등록 완료: calendarId = {}, userId = {}", calendarId, user.getId());
    }
  }

  // 참여자들의 시간대를 바탕으로 가장 많이 선택된 시간대 계산
  //  public List<TimeSlot> calculateOptimalTimes(Long calendarId) {
  //    List<AvailableTime> availableTimes = availableTimeRepository.findByCalendarId(calendarId);
  //
  //    List<TimeSlot> timeSlots = new ArrayList<>();
  //
  //    for (AvailableTime time : availableTimes) {
  //      // TimeSlot에 ID 추가
  //      TimeSlot newSlot =
  //          TimeSlot.builder()
  //              .id(time.getId()) // AvailableTime의 id를 사용
  //              .startTime(time.getStartTime())
  //              .endTime(time.getEndTime())
  //              .participants(new ArrayList<>())
  //              .build();
  //
  //      newSlot.getParticipants().add(time.getUser().getLoginId());
  //
  //      boolean merged = false;
  //
  //      for (TimeSlot existingSlot : timeSlots) {
  //        if (isOverlapping(existingSlot, newSlot)) {
  //          LocalDateTime newStartTime =
  //              existingSlot.getStartTime().isBefore(newSlot.getStartTime())
  //                  ? existingSlot.getStartTime()
  //                  : newSlot.getStartTime();
  //          LocalDateTime newEndTime =
  //              existingSlot.getEndTime().isAfter(newSlot.getEndTime())
  //                  ? existingSlot.getEndTime()
  //                  : newSlot.getEndTime();
  //
  //          existingSlot.setStartTime(newStartTime);
  //          existingSlot.setEndTime(newEndTime);
  //
  //          for (String participant : newSlot.getParticipants()) {
  //            if (!existingSlot.getParticipants().contains(participant)) {
  //              existingSlot.getParticipants().add(participant);
  //            }
  //          }
  //          merged = true;
  //          break;
  //        }
  //      }
  //
  //      if (!merged) {
  //        timeSlots.add(newSlot);
  //      }
  //    }
  //
  //    return timeSlots.stream()
  //        .sorted(
  //            (slot1, slot2) ->
  //                Integer.compare(slot2.getParticipants().size(), slot1.getParticipants().size()))
  //        .collect(Collectors.toList());
  //  }

  // 최적 시간 계산 및 합병로직
  private void mergeTimeSlots(TimeSlot existingSlot, TimeSlot newSlot) {
    existingSlot.setStartTime(
        existingSlot.getStartTime().isBefore(newSlot.getStartTime())
            ? existingSlot.getStartTime()
            : newSlot.getStartTime());

    existingSlot.setEndTime(
        existingSlot.getEndTime().isAfter(newSlot.getEndTime())
            ? existingSlot.getEndTime()
            : newSlot.getEndTime());

    // 새로운 참가자들을 기존 슬롯에 추가
    for (String participant : newSlot.getParticipants()) {
      if (!existingSlot.getParticipants().contains(participant)) {
        existingSlot.getParticipants().add(participant);
      }
    }
  }

  // 최적시간계산
  public List<TimeSlot> calculateOptimalTimes(Long calendarId) {
    List<AvailableTime> availableTimes = availableTimeRepository.findByCalendarId(calendarId);

    // AvailableTime을 TimeSlot으로 변환한 리스트
    List<TimeSlot> timeSlots =
        availableTimes.stream().map(this::toTimeSlot).collect(Collectors.toList());

    // 병합된 시간 슬롯을 담을 리스트
    List<TimeSlot> mergedTimeSlots = new ArrayList<>();

    // 시간 슬롯을 하나씩 비교하며 병합
    for (TimeSlot newSlot : timeSlots) {
      boolean merged = false;

      for (TimeSlot existingSlot : mergedTimeSlots) {
        if (isOverlapping(existingSlot, newSlot)) {
          // 겹치는 시간대가 있으면 병합
          mergeTimeSlots(existingSlot, newSlot);
          merged = true;
          break;
        }
      }

      if (!merged) {
        // 겹치는 시간대가 없으면 새로운 시간대를 추가
        mergedTimeSlots.add(newSlot);
      }
    }

    return mergedTimeSlots;
  }

  private TimeSlot toTimeSlot(AvailableTime availableTime) {
    return TimeSlot.builder()
        .startTime(availableTime.getStartTime())
        .endTime(availableTime.getEndTime())
        .participants(List.of(availableTime.getUser().getLoginId()))
        .build();
  }

  // 최적 시간 계산 및 후보 저장
  public List<TimeSlot> calculateOptimalTimesAndSave(Long calendarId) {
    log.info("최적 시간 계산 및 저장 시작. calendarId: {}", calendarId);

    List<AvailableTime> availableTimes = availableTimeRepository.findByCalendarId(calendarId);
    log.info("조회된 AvailableTime 개수: {}", availableTimes.size());

    List<TimeSlot> timeSlots = new ArrayList<>();

    for (AvailableTime time : availableTimes) {
      TimeSlot newSlot =
          TimeSlot.builder()
              .id(time.getId())
              .startTime(time.getStartTime())
              .endTime(time.getEndTime())
              .participants(new ArrayList<>())
              .build();
      newSlot.getParticipants().add(time.getUser().getLoginId());

      log.info("새로운 시간 슬롯 생성: {}", newSlot);

      // 기존 슬롯과 병합
      boolean merged = false;
      for (TimeSlot existingSlot : timeSlots) {
        if (isOverlapping(existingSlot, newSlot)) {
          mergeTimeSlots(existingSlot, newSlot); // 병합 로직
          merged = true;
          log.info("슬롯 병합 완료: {}", existingSlot);
          break;
        }
      }

      if (!merged) {
        timeSlots.add(newSlot);
        log.info("새로운 슬롯 추가: {}", newSlot);
      }
    }

    log.info("최적 시간 슬롯 개수: {}", timeSlots.size());

    // 후보 시간 저장
    for (TimeSlot slot : timeSlots) {
      OptimalTimeSlot optimalSlot = new OptimalTimeSlot();
      optimalSlot.setCalendarId(calendarId);
      optimalSlot.setStartTime(slot.getStartTime());
      optimalSlot.setEndTime(slot.getEndTime());
      optimalSlot.setParticipants(slot.getParticipants());
      optimalTimeSlotRepository.save(optimalSlot);

      log.info("OptimalTimeSlot 저장 완료: {}", optimalSlot);
    }

    return timeSlots;
  }

  // 선택한 시간 범위 내에서 시간 확정
  public void confirmCustomTimeInRange(
      Long calendarId,
      Long optimalTimeSlotId,
      LocalDateTime selectedStartTime,
      LocalDateTime selectedEndTime,
      String loginId) {

    // null 체크 추가
    if (optimalTimeSlotId == null) {
      log.error("optimalTimeSlotId is null.");
      throw new IllegalArgumentException("optimalTimeSlotId must not be null.");
    }

    if (calendarId == null) {
      log.error("calendarId is null.");
      throw new IllegalArgumentException("calendarId must not be null.");
    }

    log.info(
        "confirmCustomTimeInRange 호출됨. calendarId: {}, optimalTimeSlotId: {}, selectedStartTime: {}, selectedEndTime: {}, loginId: {}",
        calendarId,
        optimalTimeSlotId,
        selectedStartTime,
        selectedEndTime,
        loginId);

    // 최적 시간 후보에서 선택된 슬롯 확인
    OptimalTimeSlot optimalSlot =
        optimalTimeSlotRepository
            .findById(optimalTimeSlotId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "선택한 시간 슬롯을 찾을 수 없습니다. optimalTimeSlotId: " + optimalTimeSlotId));

    log.info("선택한 OptimalTimeSlot 조회 성공: {}", optimalSlot);

    // 선택한 시간 범위가 유효한지 확인
    if (selectedStartTime.isBefore(optimalSlot.getStartTime())
        || selectedEndTime.isAfter(optimalSlot.getEndTime())) {
      throw new RuntimeException(
          "선택한 시간이 범위를 벗어납니다. selectedStartTime: "
              + selectedStartTime
              + ", selectedEndTime: "
              + selectedEndTime);
    }

    // 그룹 캘린더에 시간 확정
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));
    log.info("그룹 캘린더 조회 성공: {}", calendar);

    calendar.setStartTime(selectedStartTime.toLocalTime());
    calendar.setEndTime(selectedEndTime.toLocalTime());
    calendar.setConfirmed(true);

    // 개인 캘린더 저장
    saveToUserCalendar(calendar, loginId, selectedStartTime, selectedEndTime);

    groupCalendarRepository.save(calendar);
    log.info("그룹 캘린더 저장 성공. calendarId: {}", calendarId);
  }

  // 두 시간이 겹치는지 확인하는 메서드
  private boolean isOverlapping(TimeSlot slot1, TimeSlot slot2) {
    return slot1.getStartTime().isBefore(slot2.getEndTime())
        && slot1.getEndTime().isAfter(slot2.getStartTime());
  }

  // 일정 확정 메서드 수정 (개인 캘린더 저장 추가)
  public void confirmCalendarEvent(Long calendarId, Long selectedTimeSlotId, String loginId) {
    // 최적 시간 계산 및 저장 메서드로 변경
    List<TimeSlot> optimalTimeSlots = calculateOptimalTimesAndSave(calendarId);
    TimeSlot selectedTimeSlot =
        optimalTimeSlots.stream()
            .filter(slot -> slot.getId().equals(selectedTimeSlotId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("선택한 시간 슬롯을 찾을 수 없습니다."));

    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));

    // 일정 확정
    calendar.setStartTime(selectedTimeSlot.getStartTime().toLocalTime());
    calendar.setEndTime(selectedTimeSlot.getEndTime().toLocalTime());
    calendar.setConfirmed(true);

    // 개인 캘린더에 일정 저장 (TimeSlot에서 LocalDateTime을 추출)
    saveToUserCalendar(
        calendar, loginId, selectedTimeSlot.getStartTime(), selectedTimeSlot.getEndTime());

    groupCalendarRepository.save(calendar);
    log.info("일정이 확정되었습니다: calendarId = {}", calendarId);
  }

  private Long findAppropriateCategoryId() {
    // 카테고리 ID를 결정하는 로직
    return 1L; // 예시: 기본 카테고리 ID를 반환
  }

  // 개인 캘린더에 저장하는 메서드
  private void saveToUserCalendar(
      GroupCalendar calendar, String loginId, LocalDateTime startTime, LocalDateTime endTime) {

    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    Long categoryId = findAppropriateCategoryId(); // categoryId 결정 로직

    Category category = null;
    if (categoryId != null) {
      category = categoryRepository.findById(categoryId).orElse(null); // 카테고리가 없을 경우 null 처리
    }

    uCalSaveRequestDto requestDto =
        uCalSaveRequestDto
            .builder()
            .categoryId(categoryId)
            .title(calendar.getTitle())
            .note(calendar.getNote())
            .startTime(startTime)
            .endTime(endTime)
            .build();

    // 로그 추가로 생성된 DTO의 필드 값 확인
    log.info("uCalSaveRequestDto 생성: {}", requestDto);

    userCalService.save(requestDto, user, category); // category가 null일 수 있음
  }

  // 임의 시간 확정 로직
  public void confirmCustomTime(Long calendarId, TimeSlot customTime) {
    // 임의 시간 확인 및 확정
    GroupConfirmedCalendar confirmedCalendar = new GroupConfirmedCalendar();
    confirmedCalendar.setCalendarId(calendarId);
    confirmedCalendar.setGroupId(calendarId); // groupId로 변경 필요

    // 임의 시간 설정
    confirmedCalendar.setConfirmedStartTime(customTime.getStartTime());
    confirmedCalendar.setConfirmedEndTime(customTime.getEndTime());

    groupConfirmedCalendarRepository.save(confirmedCalendar);
  }

  // 확정일정 조회
  public GroupConfirmedCalendarDTO getConfirmedCalendar(Long groupId, Long calendarId) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findByIdAndGroupIdAndConfirmed(calendarId, groupId, true) // 확정된 일정만 조회
            .orElseThrow(() -> new RuntimeException("확정된 일정을 찾을 수 없습니다: calendarId=" + calendarId));

    // GroupConfirmedCalendarDTO로 변환하여 반환
    return new GroupConfirmedCalendarDTO(
        calendar.getStartTime(),
        calendar.getEndTime(),
        calendar.getTitle(),
        calendar.getPlace(),
        calendar.getNote());
  }

  // 그룹 멤버 리스트 조회 메서드
  public List<GroupMemberResponseDTO> getGroupMembers(Long groupId) {
    List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
    return members.stream()
        .map(
            member ->
                new GroupMemberResponseDTO(
                    member.getUser().getUsername(), member.getUser().getLoginId()))
        .collect(Collectors.toList());
  }

  public void save(uCalSaveRequestDto requestDto, User user, Category category) {
    // 유저 캘린더 저장 로직
    UserCalendar userCalendar = requestDto.toEntity(user, category);

    userCalendarRepository.save(userCalendar); // 저장
  }
}
