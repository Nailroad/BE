package nailroad.domain.terms.exception;

import nailroad.domain.terms.code.TermsErrorCode;
import nailroad.global.exception.CustomException;

public class TermsException extends CustomException {
	public TermsException(TermsErrorCode errorCode) {
		super(errorCode);
	}
}
