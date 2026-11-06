package nailroad.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nailroad.global.code.BaseErrorCode;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"isSuccess", "code", "message", "result", "error"})
public class ApiResponse<T> {

	@JsonProperty("isSuccess")
	private final Boolean isSuccess;
	private final String code;
	private final String message;
	private final T result;
	private final Object error;

	public static <T> ApiResponse<T> onSuccess(String message, T result) {
		return new ApiResponse<>(true, "COMMON200", message, result, null);
	}

	public static <T> ApiResponse<T> onSuccess(String message) {
		return onSuccess(message, null);
	}

	public static <T> ApiResponse<T> onCreated(T result) {
		return new ApiResponse<>(true, "COMMON201", "생성되었습니다.", result, null);
	}

	public static <T> ApiResponse<T> onFailure(BaseErrorCode errorCode, Object error) {
		return onFailure(errorCode.getCode(), errorCode.getMessage(), error);
	}

	public static <T> ApiResponse<T> onFailure(String code, String message, Object error) {
		return new ApiResponse<>(false, code, message, null, error);
	}
}
