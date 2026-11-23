package nailroad.domain.shop.dto.request;

import java.math.BigDecimal;

public record ShopQueryRequest(
	String keyword,
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
