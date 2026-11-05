package nailroad.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {

	BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION400", "입력값이 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근 권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "지원하지 않는 HTTP 메서드입니다."),
	NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "COMMON406", "요청한 응답 형식을 지원하지 않습니다."),
	CONFLICT(HttpStatus.CONFLICT, "COMMON409", "요청이 현재 리소스 상태와 충돌합니다."),
	CONTENT_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "COMMON413", "요청 크기가 허용 범위를 초과했습니다."),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON415", "지원하지 않는 Content-Type입니다."),


	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 오류가 발생했습니다."),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON503", "일시적으로 요청을 처리할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
