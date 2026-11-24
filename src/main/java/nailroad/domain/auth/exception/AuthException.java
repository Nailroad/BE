package nailroad.domain.auth.exception;

import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.global.exception.CustomException;

public class AuthException extends CustomException {
	public AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
