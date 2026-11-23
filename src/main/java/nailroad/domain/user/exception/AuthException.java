package nailroad.domain.user.exception;

import nailroad.domain.user.code.AuthErrorCode;
import nailroad.global.exception.CustomException;

public class AuthException extends CustomException {
	public AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
