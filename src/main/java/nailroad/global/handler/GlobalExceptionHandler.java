package nailroad.global.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import nailroad.global.code.BaseErrorCode;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
		BaseErrorCode errorCode = exception.getErrorCode();
		if (errorCode.getStatus().is5xxServerError()) {
			log.error("서버 오류: {}", errorCode.getCode(), exception);
		}
		return errorResponse(errorCode, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getBindingResult().getAllErrors().forEach(error -> {
			String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
			String message = Objects.requireNonNullElse(error.getDefaultMessage(), "유효하지 않은 값입니다.");
			errors.putIfAbsent(field, message);
		});
		return errorResponse(GlobalErrorCode.VALIDATION_FAILED, errors);
	}

	@ExceptionHandler({
		HttpMessageNotReadableException.class,
		MethodArgumentTypeMismatchException.class,
		MissingServletRequestParameterException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest() {
		return errorResponse(GlobalErrorCode.BAD_REQUEST, null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
		HttpHeaders headers = new HttpHeaders();
		if (exception instanceof ErrorResponse errorResponse) {
			status = errorResponse.getStatusCode();
			headers = errorResponse.getHeaders();
		}

		if (status.is5xxServerError()) {
			log.error("서버 오류가 발생했습니다.", exception);
		}

		String message = status.is5xxServerError()
			? GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()
			: "요청을 처리할 수 없습니다.";
		return ResponseEntity.status(status).headers(headers).contentType(MediaType.APPLICATION_JSON)
			.body(ApiResponse.onFailure("COMMON" + status.value(), message, null));
	}

	private ResponseEntity<ApiResponse<Void>> errorResponse(BaseErrorCode errorCode, Object error) {
		return ResponseEntity.status(errorCode.getStatus()).contentType(MediaType.APPLICATION_JSON)
			.body(ApiResponse.onFailure(errorCode, error));
	}
}
