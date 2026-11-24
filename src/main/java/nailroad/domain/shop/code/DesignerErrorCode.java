package nailroad.domain.shop.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DesignerErrorCode implements BaseErrorCode {

	NOT_SHOP_OWNER(HttpStatus.FORBIDDEN, "DESIGNER_NOT_SHOP_OWNER403", "샵 소유자만 디자이너를 관리할 수 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
