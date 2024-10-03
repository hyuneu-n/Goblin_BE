package goblin.app.Group.service;

import java.time.LocalDate;
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
import goblin.app.Calendar.service.UserCalService;
import goblin.app.Category.model.entity.CategoryRepository;
import goblin.app.Group.model.dto.AvailableTimeRequestDTO;
import goblin.app.Group.model.dto.AvailableTimeSlot;
import goblin.app.Group.model.dto.GroupCalendarRequestDTO;
import goblin.app.Group.model.dto.GroupCalendarResponseDTO;
import goblin.app.Group.model.dto.GroupConfirmedCalendarDTO;
import goblin.app.Group.model.dto.GroupMemberResponseDTO;
import goblin.app.Group.model.dto.GroupResponseDto;
import goblin.app.Group.model.dto.TimeRange;
import goblin.app.Group.model.dto.TimeSlot;
import goblin.app.Group.model.entity.AvailableTime;
import goblin.app.Group.model.entity.Group;
import goblin.app.Group.model.entity.GroupCalendar;
import goblin.app.Group.model.entity.GroupCalendarParticipant;
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
  private final OptimalTimeSlotRepository optimalTimeSlotRepository;
  private final CategoryRepository categoryRepository;
  private final UserCalService userCalService;

  // 그룹 생성 로직
  public void createGroup(String groupName, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    // 그룹명 중복 체크
    if (groupRepository.existsByGroupName(groupName)) {
      throw new RuntimeException("이미 존재하는 그룹명입니다: groupName=" + groupName);
    }

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

    // 그룹 찾기
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

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
    groupCalendar.setGroup(group); // Group 객체 설정
    groupCalendar.setTitle(request.getTitle());
    groupCalendar.setSelectedDates(request.getDates()); // 선택된 날짜들 설정
    groupCalendar.setTime(request.getDuration()); // 예상 소요 시간 설정
    groupCalendar.setStartTime(startTime); // 시간 범위 설정
    groupCalendar.setEndTime(endTime);
    groupCalendar.setPlace(request.getPlace());
    groupCalendar.setLink(request.getLink());
    groupCalendar.setNote(request.getNote());
    groupCalendar.setCreatedDate(LocalDateTime.now());
    groupCalendar.setCreatedBy(creator); // 일정 생성자를 주최자로 설정

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
  public List<GroupResponseDto> getUserGroups(String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + loginId));

    // 유저가 속한 그룹(방장, 멤버 포함) 조회 + 삭제된 그룹은 조회 안됨
    List<Group> groups = groupRepository.findAllByUserAsMember(user);

    // Group 엔티티를 GroupResponseDto로 변환하여 반환
    return groups.stream()
        .map(
            group ->
                new GroupResponseDto(
                    group.getGroupId(), group.getGroupName(), group.getCreatedBy().getUsername()))
        .collect(Collectors.toList());
  }

  // 일정 삭제 로직
  public void deleteCalendarEvent(Long calendarId, String loginId) {
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId=" + calendarId));

    // 주최자 권한 확인
    validateEventOwner(calendar, loginId);

    calendar.setDeleted(true); // Soft Delete
    groupCalendarRepository.save(calendar);
    log.info("일정이 삭제되었습니다 (Soft Delete): calendarId = {}", calendarId);
  }

  @Transactional
  public List<GroupCalendarResponseDTO> getGroupCalendar(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));
    List<GroupCalendar> calendars = groupCalendarRepository.findAllByGroup(group);

    return calendars.stream()
        .map(
            calendar ->
                GroupCalendarResponseDTO.builder()
                    .id(calendar.getId())
                    .groupName(calendar.getGroup().getGroupName())
                    .title(calendar.getTitle())
                    .selectedDates(
                        calendar.getSelectedDates().stream()
                            .map(LocalDate::toString)
                            .collect(Collectors.toList()))
                    .time(calendar.getTime())
                    .place(calendar.getPlace())
                    .link(calendar.getLink())
                    .note(calendar.getNote())
                    .confirmed(calendar.isConfirmed())
                    .createdBy(calendar.getCreatedBy().getUsername()) // 주최자 username 반환
                    .startTime(calendar.getStartTime())
                    .endTime(calendar.getEndTime())
                    .build())
        .collect(Collectors.toList());
  }

  // 그룹 일정 수정 로직
  @Transactional
  public void updateGroupEvent(Long calendarId, GroupCalendarRequestDTO request, String loginId) {
    // 수정할 일정을 찾음
    GroupCalendar calendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다: calendarId = " + calendarId));

    // 주최자 권한 확인
    validateEventOwner(calendar, loginId);

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

    // 일정에 참가자 재설정
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

  public void setAvailableTime(Long calendarId, AvailableTimeRequestDTO request, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId = " + loginId));

    for (AvailableTimeSlot slot : request.getAvailableTimeSlots()) {
      AvailableTime availableTime = new AvailableTime();
      availableTime.setUser(user); // User 객체 설정
      availableTime.setCalendarId(calendarId);

      // AM/PM 정보를 포함하여 시간 변환
      LocalTime startTime =
          convertToLocalTime(slot.getStartAmPm(), slot.getStartHour(), slot.getStartMinute());
      LocalTime endTime =
          convertToLocalTime(slot.getEndAmPm(), slot.getEndHour(), slot.getEndMinute());

      LocalDateTime startDateTime = LocalDateTime.of(slot.getDate(), startTime);
      LocalDateTime endDateTime = LocalDateTime.of(slot.getDate(), endTime);

      availableTime.setStartTime(startDateTime);
      availableTime.setEndTime(endDateTime);

      availableTimeRepository.save(availableTime);
      log.info("참여자의 가능 시간 등록 완료: calendarId = {}, userId = {}", calendarId, user.getId());
    }
  }

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

    // AM/PM 형식으로 시간 변환
    int startHour = selectedStartTime.getHour();
    int startMinute = selectedStartTime.getMinute();
    String amPmStart = (startHour >= 12) ? "PM" : "AM";
    if (startHour > 12) startHour -= 12;

    int endHour = selectedEndTime.getHour();
    int endMinute = selectedEndTime.getMinute();
    String amPmEnd = (endHour >= 12) ? "PM" : "AM";
    if (endHour > 12) endHour -= 12;

    // 개인 캘린더 저장
    saveToUserCalendar(
        calendar, loginId, amPmStart, startHour, startMinute, amPmEnd, endHour, endMinute);

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

    // 주최자 권한 확인
    validateEventOwner(calendar, loginId);

    // 일정 확정
    calendar.setStartTime(selectedTimeSlot.getStartTime().toLocalTime());
    calendar.setEndTime(selectedTimeSlot.getEndTime().toLocalTime());
    calendar.setConfirmed(true);

    // TimeSlot에서 LocalDateTime을 AM/PM 형식으로 변환
    int startHour = selectedTimeSlot.getStartTime().getHour();
    int startMinute = selectedTimeSlot.getStartTime().getMinute();
    String amPmStart = (startHour >= 12) ? "PM" : "AM";
    if (startHour > 12) startHour -= 12;

    int endHour = selectedTimeSlot.getEndTime().getHour();
    int endMinute = selectedTimeSlot.getEndTime().getMinute();
    String amPmEnd = (endHour >= 12) ? "PM" : "AM";
    if (endHour > 12) endHour -= 12;

    // 개인 캘린더에 일정 저장
    saveToUserCalendar(
        calendar, loginId, amPmStart, startHour, startMinute, amPmEnd, endHour, endMinute);

    groupCalendarRepository.save(calendar);
    log.info("일정이 확정되었습니다: calendarId = {}", calendarId);
  }

  private void saveToUserCalendar(
      GroupCalendar calendar,
      String loginId,
      String amPmStart,
      int startHour,
      int startMinute,
      String amPmEnd,
      int endHour,
      int endMinute) {

    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: loginId=" + loginId));

    // DTO 생성 시 AM/PM 값과 시간을 전달
    uCalSaveRequestDto requestDto =
        uCalSaveRequestDto
            .builder()
            .title(calendar.getTitle())
            .note(calendar.getNote())
            .amPmStart(amPmStart)
            .startHour(startHour)
            .startMinute(startMinute)
            .amPmEnd(amPmEnd)
            .endHour(endHour)
            .endMinute(endMinute)
            .build();

    log.info("uCalSaveRequestDto 생성: {}", requestDto);

    userCalService.save(requestDto, user);
  }

  // 확정일정 조회
  public GroupConfirmedCalendarDTO getConfirmedCalendar(Long groupId, Long calendarId) {
    // 먼저 groupId로 Group 객체를 조회합니다.
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

    // 조회한 Group 객체와 함께 일정 정보를 조회합니다.
    GroupCalendar calendar =
        groupCalendarRepository
            .findByIdAndGroupAndConfirmed(calendarId, group, true) // 확정된 일정만 조회
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

  // 주최자 권한 확인 메서드
  private void validateEventOwner(GroupCalendar calendar, String loginId) {
    if (!calendar.getCreatedBy().getLoginId().equals(loginId)) {
      throw new RuntimeException("해당 일정의 주최자가 아닙니다.");
    }
  }
}
