package nailroad.domain.design.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DesignErrorCode implements BaseErrorCode {
    INVALID_FILTER(HttpStatus.BAD_REQUEST, "DESIGN_INVALID_FILTER400", "지원하지 않는 디자인 필터 값입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "INVALID_CURSOR400", "올바르지 않은 커서입니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE400", "조회 개수는 1 이상 50 이하의 정수여야 합니다."),
    INVALID_DESIGN_ID(HttpStatus.BAD_REQUEST, "DESIGN_INVALID_ID400", "올바르지 않은 디자인 ID입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "DESIGN_UNAUTHORIZED401", "인증이 필요합니다."),
    DESIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "DESIGN_NOT_FOUND404", "디자인을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
