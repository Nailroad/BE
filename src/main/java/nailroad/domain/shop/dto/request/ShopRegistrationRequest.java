package nailroad.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties({"latitude", "longitude"})
public record ShopRegistrationRequest(
	@JsonDeserialize(using = TextValue.class)
	@NotBlank @Size(max = 255) String shopName,
	@JsonDeserialize(using = TextValue.class)
	@NotBlank @Pattern(regexp = "(?:010-?[0-9]{4}|(?:02|0[3-6][0-9])-?[0-9]{3,4})-?[0-9]{4}") String shopPhone,
	@JsonDeserialize(using = TextValue.class)
	@NotBlank @Size(max = 255) String shopAddress,
	@JsonDeserialize(using = TextValue.class)
	@Size(max = 255) String detailAddress
) {

	public ShopRegistrationRequest {
		shopName = strip(shopName);
		shopPhone = strip(shopPhone);
		shopAddress = strip(shopAddress);
		detailAddress = strip(detailAddress);
	}

	private static String strip(String value) {
		return value == null ? null : value.strip();
	}

	// JSON 타입 오류와 값 검증 오류를 구분하기 위해 이 요청에서만 자동 형변환을 막습니다.
	static final class TextValue extends ValueDeserializer<String> {
		@Override
		public String deserialize(JsonParser parser, DeserializationContext context) {
			if (!parser.hasToken(JsonToken.VALUE_STRING)) {
				return context.reportInputMismatch(String.class, "문자열이 필요합니다.");
			}
			return parser.getString();
		}
	}
}
