package nailroad.domain.shop.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode implements BaseErrorCode {

	DUPLICATE_REGISTRATION(HttpStatus.CONFLICT, "SHOP_DUPLICATE_REGISTRATION409", "이미 심사 중이거나 승인된 동일한 샵이 있습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "SHOP_NOT_FOUND404", "샵을 찾을 수 없습니다."),
	NOT_OWNER(HttpStatus.FORBIDDEN, "SHOP_NOT_OWNER403", "샵 소유자만 정보를 수정할 수 있습니다."),
	NOT_APPROVED(HttpStatus.CONFLICT, "SHOP_NOT_APPROVED409", "승인된 샵만 정보를 수정할 수 있습니다."),
	INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "SHOP_INVALID_IMAGE_KEY400", "이 샵에 등록할 수 없는 이미지 키입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
