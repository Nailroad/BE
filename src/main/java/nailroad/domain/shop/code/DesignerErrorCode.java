package nailroad.domain.shop.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DesignerErrorCode implements BaseErrorCode {

	NOT_SHOP_OWNER(HttpStatus.FORBIDDEN, "DESIGNER_NOT_SHOP_OWNER403", "샵 소유자만 디자이너를 관리할 수 있습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "DESIGNER_NOT_FOUND404", "샵에서 디자이너를 찾을 수 없습니다."),
	IN_USE(HttpStatus.CONFLICT, "DESIGNER_IN_USE409", "연결된 데이터가 있는 디자이너는 삭제할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
