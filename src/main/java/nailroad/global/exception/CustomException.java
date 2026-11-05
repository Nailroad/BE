package nailroad.global.exception;

import java.util.Objects;

import lombok.Getter;
import nailroad.global.code.BaseErrorCode;

@Getter
public class CustomException extends RuntimeException {

	private final BaseErrorCode errorCode;

	public CustomException(BaseErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
