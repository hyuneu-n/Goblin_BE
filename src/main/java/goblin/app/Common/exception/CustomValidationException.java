package goblin.app.Common.exception;

import java.util.Map;

// 여러 필드의 유효성 검증 실패 시 사용하는 예외
public class CustomValidationException extends RuntimeException {

  private final Map<String, String> errors; // 필드별 오류 메시지를 담는 맵

  public CustomValidationException(Map<String, String> errors) {
    super("Validation failed");
    this.errors = errors;
  }

  // 오류 목록 반환
  public Map<String, String> getErrors() {
    return errors;
  }
}
