package nailroad.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties({"latitude", "longitude"})
public record ShopRegistrationRequest(
	@NotBlank @Size(max = 255) String shopName,
	@JsonProperty("shop_owner_phone")
	@Schema(description = "사장님 개인 휴대폰 번호")
	@NotBlank @Pattern(regexp = "010-?[0-9]{4}-?[0-9]{4}") String shopOwnerPhone,
	@Schema(description = "고객에게 공개할 매장 전화번호")
	@NotBlank @Pattern(regexp = ShopRegistrationRequest.SHOP_PHONE_PATTERN) String shopPhone,
	@NotBlank @Size(max = 255) String shopAddress,
	@Size(max = 255) String detailAddress
) {
	static final String SHOP_PHONE_PATTERN = "(?:02|0[3-6][0-9])-?[0-9]{3,4}-?[0-9]{4}";
}
