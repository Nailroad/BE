package nailroad.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties({"latitude", "longitude"})
public record ShopRegistrationRequest(
	@NotBlank @Size(max = 255) String shopName,
	@NotBlank @Pattern(regexp = "(?:010-?[0-9]{4}|(?:02|0[3-6][0-9])-?[0-9]{3,4})-?[0-9]{4}") String shopPhone,
	@NotBlank @Size(max = 255) String shopAddress,
	@Size(max = 255) String detailAddress
) {
}
