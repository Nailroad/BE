package nailroad.domain.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record ShopQueryRequest(
	String keyword,
	@Schema(description = "같은 조회 조건에서 이전 응답의 nextCursor에 담긴 마지막 샵 ID")
	Long cursor,
	Integer size,
	BigDecimal south,
	BigDecimal north,
	BigDecimal west,
	BigDecimal east,
	Sort sort
) {

	public enum Sort {
		LATEST,
		DISTANCE,
		RATING,
		REVIEW_COUNT
	}

	public boolean hasMapBounds() {
		return south != null || north != null || west != null || east != null;
	}

	public boolean hasValidMapBounds() {
		return south != null && north != null && west != null && east != null
			&& south.compareTo(BigDecimal.valueOf(-90)) >= 0 && north.compareTo(BigDecimal.valueOf(90)) <= 0
			&& west.compareTo(BigDecimal.valueOf(-180)) >= 0 && east.compareTo(BigDecimal.valueOf(180)) <= 0
			&& south.compareTo(north) <= 0 && west.compareTo(east) <= 0;
	}
}
