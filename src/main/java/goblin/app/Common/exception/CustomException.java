package goblin.app.Common.exception;

public class CustomException extends RuntimeException {
  private ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getDetail());
    this.errorCode = errorCode;
  }


  public CustomException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

}
