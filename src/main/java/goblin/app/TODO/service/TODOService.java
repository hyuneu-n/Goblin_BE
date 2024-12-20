package goblin.app.TODO.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import goblin.app.Group.model.entity.Group;
import goblin.app.Group.repository.GroupRepository;
import goblin.app.TODO.model.dto.TODORequestDTO;
import goblin.app.TODO.model.dto.TODOResponseDTO;
import goblin.app.TODO.model.entity.TODO;
import goblin.app.TODO.repository.TODORepository;
import goblin.app.User.util.JwtUtil;
import io.jsonwebtoken.Claims;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TODOService {

  private final TODORepository todoRepository;
  private final GroupRepository groupRepository;
  private final JwtUtil jwtUtil;

  public TODOResponseDTO createTODO(Long groupId, TODORequestDTO request, String loginId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

    TODO newTodo =
        TODO.builder()
            .group(group)
            .task(request.getTask())
            .dueDate(request.getDueDate())
            .completed(false)
            .build();

    TODO savedTodo = todoRepository.save(newTodo);

    return convertToDTO(savedTodo);
  }

  public void markCompleted(Long todoId, String loginId) {
    TODO todo =
        todoRepository.findById(todoId).orElseThrow(() -> new RuntimeException("TODO not found"));

    todo.markAsCompleted();
    todoRepository.save(todo);
  }

  @Transactional
  public List<TODOResponseDTO> getPendingTODOs(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다: groupId=" + groupId));

    List<TODO> pendingTODOs = todoRepository.findAllByGroupAndCompletedFalse(group);

    return pendingTODOs.stream()
        .map(
            todo -> {
              LocalDateTime createdDateTime = todo.getCreatedDate().atTime(0, 0); // 00:00:00
              LocalDateTime dueDateTime = todo.getDueDate().atTime(23, 59); // 23:59:00

              return new TODOResponseDTO(
                  todo.getId(),
                  todo.getTask(),
                  createdDateTime,
                  dueDateTime,
                  todo.isCompleted(),
                  calculateDDay(dueDateTime), // D-day 계산
                  group.getGroupName());
            })
        .collect(Collectors.toList());
  }

  public List<TODOResponseDTO> getCompletedTODOs(Long groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

    return todoRepository.findAllByGroupAndCompletedTrue(group).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  private TODOResponseDTO convertToDTO(TODO todo) {
    LocalDateTime createdDateTime = todo.getCreatedDate().atStartOfDay(); // 00:00:00
    LocalDateTime dueDateTime = todo.getDueDate().atTime(23, 59); // 23:59:00

    return new TODOResponseDTO(
        todo.getId(),
        todo.getTask(),
        createdDateTime,
        dueDateTime,
        todo.isCompleted(),
        calculateDDay(dueDateTime), // D-day 계산
        todo.getGroup().getGroupName());
  }

  public void cancelCompleted(Long todoId, String token) {
    String loginId = extractLoginId(token);

    TODO todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new RuntimeException("TODO를 찾을 수 없습니다: todoId = " + todoId));

    // TODO가 완료 상태인지 확인
    if (!todo.isCompleted()) {
      throw new RuntimeException("이미 완료되지 않은 TODO입니다.");
    }

    // 완료 상태 취소
    todo.setCompleted(false);
    todoRepository.save(todo);

    log.info("TODO 완료 상태 취소 완료: todoId = {}, userId = {}", todoId, loginId);
  }

  public TODOResponseDTO updateTODO(Long todoId, TODORequestDTO request, String loginId) {
    TODO todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new RuntimeException("해당 TODO를 찾을 수 없습니다."));

    // 제목과 마감일 수정
    todo.setTask(request.getTask());
    todo.setDueDate(request.getDueDate());

    todoRepository.save(todo);

    return convertToDTO(todo);
  }

  public void deleteTODO(Long todoId) {
    TODO todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new RuntimeException("해당 TODO를 찾을 수 없습니다."));

    todoRepository.delete(todo);
  }

  // 날짜 범위에 해당하는 TODO 목록 조회
  public List<TODOResponseDTO> getTODOsByDateRange(Long groupId, LocalDate date) {
    List<TODO> todos = todoRepository.findByGroupIdAndDateRange(groupId, date);

    return todos.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  private String extractLoginId(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7);
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      return claims.getId(); // 토큰에서 loginId 추출
    }
    return null;
  }

  private int calculateDDay(LocalDateTime dueDateTime) {
    LocalDateTime now = LocalDateTime.now();
    return (int) java.time.Duration.between(now, dueDateTime).toDays();
  }
}
