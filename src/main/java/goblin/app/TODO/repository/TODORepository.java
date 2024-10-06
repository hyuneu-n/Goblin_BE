package goblin.app.TODO.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import goblin.app.Group.model.entity.Group;
import goblin.app.TODO.model.entity.TODO;

public interface TODORepository extends JpaRepository<TODO, Long> {

  @Query(
      "SELECT t FROM TODO t WHERE t.group.groupId = :groupId AND :targetDate BETWEEN t.createdDate AND t.dueDate")
  List<TODO> findByGroupIdAndDateRange(
      @Param("groupId") Long groupId, @Param("targetDate") LocalDate targetDate);

  // 완료되지 않은 TODO 목록을 조회
  List<TODO> findAllByGroupAndCompletedFalse(Group group);

  // 완료된 TODO 목록을 조회
  List<TODO> findAllByGroupAndCompletedTrue(Group group);
}
