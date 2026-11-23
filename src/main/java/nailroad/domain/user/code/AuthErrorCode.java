package nailroad.domain.user.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED401", "로그인이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN403", "접근 권한이 없습니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN401", "유효하지 않거나 만료된 로그인 토큰입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH401", "유효하지 않거나 만료된 재발급 토큰입니다."),
	USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_USER_NOT_FOUND401", "로그인한 사용자를 찾을 수 없습니다."),
	KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_KAKAO_LOGIN401", "카카오 로그인을 완료하지 못했습니다."),
	ONBOARDING_ALREADY_COMPLETE(HttpStatus.CONFLICT, "AUTH_ONBOARDING409", "이미 회원가입을 완료했습니다."),
	KAKAO_USER_ID_MISSING(HttpStatus.BAD_GATEWAY, "AUTH_KAKAO_USER502", "카카오 사용자 ID를 확인할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
