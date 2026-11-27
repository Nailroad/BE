package nailroad.domain.terms.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermsErrorCode implements BaseErrorCode {
	NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "TERMS_NOT_CONFIGURED503", "가입 약관이 준비되지 않았습니다."),
	UNKNOWN_TERM(HttpStatus.BAD_REQUEST, "TERMS_UNKNOWN400", "알 수 없는 약관 항목입니다."),
	DUPLICATE_TERM(HttpStatus.BAD_REQUEST, "TERMS_DUPLICATE400", "약관 항목을 중복 제출했습니다."),
	REQUIRED_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_REQUIRED400", "필수 약관에 동의해 주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
