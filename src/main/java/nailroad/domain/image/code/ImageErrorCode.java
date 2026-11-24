package nailroad.domain.image.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
    INVALID_PURPOSE(HttpStatus.BAD_REQUEST, "IMAGE_INVALID_PURPOSE400", "올바르지 않은 이미지 용도입니다."),
    INVALID_FILE_SIZE(HttpStatus.BAD_REQUEST, "IMAGE_INVALID_FILE_SIZE400", "이미지 파일의 크기는 1바이트 이상이어야 합니다."),
    FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "IMAGE_FILE_TOO_LARGE413", "이미지 파일은 5MB 이하만 업로드할 수 있습니다."),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_UNSUPPORTED_CONTENT_TYPE400", "지원하지 않는 이미지 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
