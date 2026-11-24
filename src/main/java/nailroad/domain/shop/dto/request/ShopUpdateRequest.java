package nailroad.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import nailroad.domain.shop.entity.Shop.DayHours;
import nailroad.domain.shop.enums.ShopFacility;
import nailroad.domain.shop.enums.ShopStatus;

public record ShopUpdateRequest(
	@Pattern(regexp = ShopRegistrationRequest.SHOP_PHONE_PATTERN) String shopPhone,
	Map<DayOfWeek, @NotNull @Valid DayHours> businessHours,
	ShopStatus status,
	List<@NotNull ShopFacility> facilities,
	List<@NotNull @Size(max = 1024) @Pattern(regexp = "shops/[1-9][0-9]*/[A-Za-z0-9][A-Za-z0-9._-]*") String> imageKeys
) {
	@JsonIgnore
	@AssertTrue(message = "수정할 샵 정보를 한 항목 이상 입력해 주세요.")
	public boolean isUpdateRequested() {
		return shopPhone != null || businessHours != null || status != null || facilities != null || imageKeys != null;
	}
}
