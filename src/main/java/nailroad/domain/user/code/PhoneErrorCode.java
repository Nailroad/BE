package nailroad.domain.user.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PhoneErrorCode implements BaseErrorCode {
	INVALID_PHONE(HttpStatus.BAD_REQUEST, "PHONE_INVALID400", "올바른 휴대전화 번호를 입력해 주세요."),
	CODE_INVALID(HttpStatus.BAD_REQUEST, "PHONE_CODE_INVALID400", "인증번호가 올바르지 않습니다."),
	CODE_EXPIRED(HttpStatus.BAD_REQUEST, "PHONE_CODE_EXPIRED400", "인증번호가 만료되었습니다."),
	NOT_VERIFIED(HttpStatus.BAD_REQUEST, "PHONE_NOT_VERIFIED400", "휴대전화 인증을 완료해 주세요."),
	TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "PHONE_ATTEMPTS429", "잠시 후 다시 인증해 주세요."),
	SEND_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "PHONE_SEND429", "인증번호 재발송은 잠시 후 가능합니다."),
	DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "PHONE_DAILY_LIMIT429",
		"인증번호 발송 횟수를 초과했습니다. 24시간 후 다시 시도해 주세요."),
	DELIVERY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "PHONE_DELIVERY503", "문자 발송 서비스를 사용할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
