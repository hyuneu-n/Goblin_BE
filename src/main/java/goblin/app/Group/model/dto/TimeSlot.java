package goblin.app.Group.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TimeSlot {
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private List<String> participants;

  public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, List<String> participants) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.participants = participants;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public List<String> getParticipants() {
    return participants;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TimeSlot timeSlot = (TimeSlot) o;
    return Objects.equals(startTime, timeSlot.startTime)
        && Objects.equals(endTime, timeSlot.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, endTime);
  }
}
