package nailroad.domain.user.exception;

import nailroad.domain.user.code.PhoneErrorCode;
import nailroad.global.exception.CustomException;

public class PhoneException extends CustomException {
	public PhoneException(PhoneErrorCode errorCode) {
		super(errorCode);
	}
}
