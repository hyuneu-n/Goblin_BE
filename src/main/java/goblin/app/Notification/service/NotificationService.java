package goblin.app.Notification.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import goblin.app.Group.model.entity.GroupCalendar;
import goblin.app.Group.model.entity.GroupCalendarParticipant;
import goblin.app.Group.model.entity.GroupConfirmedCalendar;
import goblin.app.Group.repository.GroupCalendarParticipantRepository;
import goblin.app.Group.repository.GroupCalendarRepository;
import goblin.app.Group.repository.GroupConfirmedCalendarRepository;
import goblin.app.Notification.model.dto.NotificationDto;
import goblin.app.Notification.model.dto.NotificationResponseDto;
import goblin.app.Notification.model.entity.EmitterRepository;
import goblin.app.Notification.model.entity.Notification;
import goblin.app.Notification.model.entity.NotificationRepository;
import goblin.app.Notification.model.entity.NotificationType;
import goblin.app.User.model.entity.User;
import goblin.app.User.repository.UserRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {

  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  private final GroupCalendarParticipantRepository groupCalendarParticipantRepository;

  private final GroupConfirmedCalendarRepository groupConfirmedCalendarRepository;

  private final GroupCalendarRepository groupCalendarRepository;

  // 기본 타임아웃 10분 설정
  private static final Long DEFAULT_TIMEOUT = 600000L;

  // SSE 구독 메서드
  public SseEmitter eventN(Long userId) {
    // 타임아웃을 1시간으로 설정
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT); // 1시간 타임아웃
    emitterRepository.save(userId, emitter);

    // 클라이언트로 초기 더미 데이터 전송
    try {
      User user =
          userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

      // createDummyNotification 메서드를 사용해 더미 알림 생성
      Notification dummyNotification = createDummyNotification(user);

      // 더미 알림 데이터를 클라이언트로 전송
      emitter.send(SseEmitter.event().name("INIT").data(dummyNotification));
      log.info("Dummy notification sent to user with userId: {}", userId);

      // 주기적으로 keep-alive 메시지 전송 (30초마다)
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      scheduler.scheduleAtFixedRate(
          () -> {
            try {
              emitter.send(SseEmitter.event().name("keep-alive").data("keep-alive"));
              log.info("Keep-alive message sent to user with userId: {}", userId);
            } catch (IOException e) {
              log.error("Error while sending keep-alive message to userId: {}", userId, e);
              emitter.completeWithError(e);
              emitterRepository.deleteById(userId); // 에러 발생 시 emitter 제거
            }
          },
          0,
          30,
          TimeUnit.SECONDS); // 30초 간격으로 keep-alive 메시지 전송

    } catch (IOException e) {
      log.error("Error in eventN for userId: {}", userId, e);
      emitter.completeWithError(e);
    }

    emitter.onCompletion(
        () -> {
          log.info("Emitter for userId {} completed", userId);
          emitterRepository.deleteById(userId);
        });

    emitter.onTimeout(
        () -> {
          log.info("Emitter for userId {} timed out", userId);
          emitterRepository.deleteById(userId);
        });

    emitter.onError(
        (e) -> {
          log.error("Emitter for userId {} encountered error: {}", userId, e.getMessage());
          emitterRepository.deleteById(userId);
        });

    return emitter;
  }

  // 더미데이터를 생성
  private Notification createDummyNotification(User user) {
    return Notification.builder()
        .user(user)
        .msgTitle("Dummy Notification")
        .eventName("Dummy Event")
        .details1("send dummy data to client.")
        .details2("This is a dummy notification.")
        .type(NotificationType.DUMMY)
        .build();
  }

  public NotificationResponseDto sendNotification(String loginId, NotificationDto dto) {
    User user = getUserByLoginId(loginId);
    Long userId = user.getId();

    // Notification 저장
    Notification notification = notificationRepository.save(dto.ToEntity(user));
    log.info("Notification created for user {}: {}", loginId, notification);

    // 알림 전송
    SseEmitter emitter = emitterRepository.get(userId);
    if (emitter != null) {
      try {
        NotificationResponseDto responseDto = new NotificationResponseDto(notification);
        emitter.send(SseEmitter.event().name("notification").data(responseDto));
        log.info("Sent notification to userId {}: {}", userId, responseDto);
      } catch (IOException e) {
        log.error("Failed to send notification to emitter {}: {}", userId, e.getMessage());
        emitterRepository.deleteById(userId);
        emitter.completeWithError(e);
      }
    } else {
      log.warn("No emitter found for user ID: {}. Notification will be queued.", userId);
      // 필요하다면 알림을 큐에 저장하여 나중에 전송하도록 개선 가능
    }
    return new NotificationResponseDto(notification);
  }

  // 커스텀 알림 전송
  public <T> void customNotify(Long userId, T data, String comment, String type) {
    sendToClient(userId, data, comment, type);
  }

  // 일반 알림 전송
  public void notify(Long userId, Object data, String comment) {
    sendToClient(userId, data, comment, "sse");
  }

  // Emitter 생성 메서드
  private SseEmitter createEmitter(Long userId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(userId, emitter);

    emitter.onCompletion(() -> emitterRepository.deleteById(userId));
    emitter.onTimeout(() -> emitterRepository.deleteById(userId));

    return emitter;
  }

  // 클라이언트에게 데이터 전송 메서드 (다형성 통합)
  private <T> void sendToClient(Long userId, T data, String comment, String type) {
    SseEmitter emitter = emitterRepository.get(userId);
    if (emitter != null) {
      try {
        emitter.send(
            SseEmitter.event().id(String.valueOf(userId)).name(type).data(data).comment(comment));
        log.info("Sent event to emitter {}: {}", userId, data);
      } catch (IOException e) {
        emitterRepository.deleteById(userId);
        emitter.completeWithError(e);
        log.error("Failed to send event to emitter {}: {}", userId, e.getMessage());
      }
    } else {
      log.warn("No emitter found for user ID: {}", userId);
    }
  }

  // 클라이언트에게 알림 전송 메서드 (별도의 emitter 전달)
  private synchronized void sendToClient(
      SseEmitter emitter, String emitterId, Object data, String type) {
    try {
      emitter.send(
          SseEmitter.event().id(emitterId).name(type).data(data, MediaType.APPLICATION_JSON));
      log.info("Sent event to emitter {}: {}", emitterId, data);
    } catch (IOException e) {
      emitter.completeWithError(e);
      emitterRepository.deleteById(Long.parseLong(emitterId.split("_")[0]));
      log.error("Failed to send event to emitter {}: {}", emitterId, e.getMessage());
    }
  }

  // 유저 확인 및 반환
  private User getUserByLoginId(String loginId) {
    return userRepository
        .findByLoginId(loginId)
        .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
  }

  // 일정 확정 시 그룹 이벤트의 모든 참여자에게 알림 전송
  public void eventFixedNotify(Long calendarId) {
    GroupConfirmedCalendar groupEvent =
        groupConfirmedCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
    Long duration =
        java.time.Duration.between(
                groupEvent.getConfirmedStartTime(), groupEvent.getConfirmedEndTime())
            .toMinutes();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy년 MM월 dd일");
    String fixedDate = groupEvent.getConfirmedStartTime().format(formatter);

    GroupCalendar groupCalendar =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

    // NotificationDto 생성
    NotificationDto dto = new NotificationDto();
    dto.setMsgTitle("모임 일정이 확정됐어요!");
    dto.setEventName(groupCalendar.getTitle());
    dto.setDetails1(fixedDate);
    dto.setDetails2(duration.toString());
    dto.setType(NotificationType.EVENT_CREATED); // 알림 유형 설정

    // 그룹 캘린더 참가자 목록
    List<GroupCalendarParticipant> participants =
        groupCalendarParticipantRepository.findByCalendarId(groupEvent.getCalendarId());

    // 각 참가자에게 알림 전송
    for (GroupCalendarParticipant participant : participants) {
      Long userId = participant.getUserId(); // userId 추출
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
      if (user != null) {
        // 알림 생성 및 저장
        Notification notification = notificationRepository.save(dto.ToEntity(user));
        // SseEmitter를 통해 사용자에게 알림 전송
        sendNotification(user.getLoginId(), dto);
      } else {
        log.warn("User not found for ID: {}", userId);
      }
    }
  }

  // 소요 시간을 포맷팅하는 메서드
  private String formatDuration(int time) {
    int hours = time / 60;
    int minutes = time % 60;
    return String.format("%d시간 %d분", hours, minutes);
  }

  // 이벤트 기간을 포맷팅하는 메서드
  private String formatEventPeriod(List<LocalDate> dayList) {
    if (dayList == null || dayList.isEmpty()) {
      throw new IllegalArgumentException("dayList는 null이거나 비어있을 수 없습니다.");
    }

    LocalDate startDate = dayList.get(0); // 첫 번째 날짜
    LocalDate endDate = dayList.get(dayList.size() - 1); // 마지막 날짜

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
    return startDate.format(formatter) + " ~ " + endDate.format(formatter);
  }

  // 이벤트 생성 시 가능한 시간 설정 알림, 모든 참여자에게
  public void eventCreatedNotify(Long calendarId) {

    GroupCalendar groupEvent =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

    // 이벤트 시간이 포맷팅된 문자열 생성
    String duration = formatDuration(groupEvent.getTime());
    String period = formatEventPeriod(groupEvent.getSelectedDates());

    // NotificationDto 생성
    NotificationDto dto = new NotificationDto();
    dto.setMsgTitle("새로운 모임 일정이 왔어요!");
    dto.setEventName(groupEvent.getTitle());
    dto.setDetails1(period);
    dto.setDetails2(duration);
    dto.setType(NotificationType.EVENT_CREATED); // 알림 유형 설정

    // 그룹 캘린더 참가자 목록
    List<GroupCalendarParticipant> participants =
        groupCalendarParticipantRepository.findByCalendarId(groupEvent.getId());

    // 각 참가자에게 알림 전송
    for (GroupCalendarParticipant participant : participants) {
      Long userId = participant.getUserId(); // userId 추출
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
      if (user != null) {
        // 알림 생성 및 저장
        Notification notification = notificationRepository.save(dto.ToEntity(user));

        // SseEmitter를 통해 사용자에게 알림 전송
        sendNotification(user.getLoginId(), dto);
      } else {
        log.warn("User not found for ID: {}", userId);
      }
    }
  }

  // 주최자에게 일정 확정 알림, 모든 사용자가 완료하였을 때, 즉 모든 참여자가 일정을 확정했을 때
  public void eventSelectedNotify(Long calendarId) {
    //    if(groupEvent.isConfirmed() == true) 일때 이 메서드를 실행
    GroupCalendar groupEvent =
        groupCalendarRepository
            .findById(calendarId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
    String duration = formatDuration(groupEvent.getTime());
    String period = formatEventPeriod(groupEvent.getSelectedDates());
    User creator = groupEvent.getCreatedBy();
    NotificationDto dto = new NotificationDto();
    dto.setMsgTitle("일정을 확정해주세요!");
    dto.setEventName(groupEvent.getTitle());
    dto.setDetails1(period);
    dto.setDetails2(duration);
    dto.setType(NotificationType.MUST_FIX_EVENT); // 알림 유형 설정

    // 알림 생성 및 저장
    Notification notification = notificationRepository.save(dto.ToEntity(creator));

    // SseEmitter를 통해 사용자에게 알림 전송
    sendNotification(creator.getLoginId(), dto);
  }
}
