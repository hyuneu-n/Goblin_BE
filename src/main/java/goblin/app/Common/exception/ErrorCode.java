package goblin.app.Common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  // 공통 예외
  BAD_REQUEST_PARAM(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  BAD_REQUEST_VALIDATION(HttpStatus.BAD_REQUEST, "검증에 실패하였습니다."),

  // 카테고리
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "검증에 실패하였습니다."),

    // 달력
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "작성자가 아닙니다."),


  // 토큰 예외
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
  // 경로 예외
  NOT_VALID_URI(HttpStatus.BAD_REQUEST, "유효한 경로로 요청해주세요.");

    private HttpStatus status;
    private String detail;


}
