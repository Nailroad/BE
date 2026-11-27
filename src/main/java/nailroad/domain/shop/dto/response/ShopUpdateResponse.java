package nailroad.domain.shop.dto.response;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import nailroad.domain.shop.entity.Shop.DayHours;
import nailroad.domain.shop.enums.ShopFacility;
import nailroad.domain.shop.enums.ShopStatus;

public record ShopUpdateResponse(
	Long shopId,
	String shopPhone,
	Map<DayOfWeek, DayHours> businessHours,
	ShopStatus status,
	List<ShopFacility> facilities,
	List<String> imageKeys
) {
}
