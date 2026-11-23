package nailroad.domain.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import nailroad.domain.shop.entity.Shop.DayHours;
import nailroad.domain.shop.enums.ShopFacility;
import nailroad.domain.shop.enums.ShopStatus;

public final class ShopQueryResponse {

	private ShopQueryResponse() {
	}

	@Schema(name = "ShopListResponse")
	public record ListResponse(List<Item> items, int size, boolean hasNext, Long nextCursor) {

		public record Item(
			Long shopId,
			String shopName,
			String shopAddress,
			BigDecimal latitude,
			BigDecimal longitude,
			ShopStatus status,
			List<String> imageUrls,
			Double averageRating,
			long reviewCount,
			boolean isLiked
		) {
		}
	}

	@Schema(name = "ShopDetailResponse")
	public record DetailResponse(
		Long shopId,
		String shopName,
		String shopPhone,
		String shopAddress,
		String detailAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		String location,
		ShopStatus status,
		Map<DayOfWeek, DayHours> businessHours,
		List<ShopFacility> facilities,
		List<String> imageUrls,
		List<Price> prices,
		Double averageRating,
		long reviewCount,
		boolean isLiked
	) {

		public record Price(Long shopPriceId, String serviceCategory, String serviceName, Integer servicePrice) {
		}
	}
}
