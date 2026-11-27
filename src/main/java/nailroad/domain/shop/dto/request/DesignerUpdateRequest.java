package nailroad.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DesignerUpdateRequest(
	@Size(max = 255)
	@Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "이름을 입력해 주세요.")
	String name,
	@Schema(description = "프로필 사진을 변경할 때 사용하는 새 DESIGNER 이미지 키")
	@Pattern(regexp = DesignerRegistrationRequest.PROFILE_IMAGE_KEY_PATTERN,
		message = "디자이너 프로필 이미지 키를 확인해 주세요.")
	String profileImageKey,
	String description,
	@Size(max = 255) String specialties
) {
	@JsonIgnore
	@AssertTrue(message = "수정할 디자이너 정보를 한 항목 이상 입력해 주세요.")
	public boolean isUpdateRequested() {
		return name != null || profileImageKey != null || description != null || specialties != null;
	}
}
