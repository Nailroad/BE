package nailroad.domain.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DesignerRegistrationRequest(
	@NotBlank @Size(max = 255) String name,
	@Schema(description = "DESIGNER 용도로 발급받아 업로드한 프로필 이미지 키")
	@Pattern(regexp = "designers/[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}\\.[a-z]{3,4}",
		message = "디자이너 프로필 이미지 키를 확인해 주세요.")
	String profileImageKey,
	String description,
	@Size(max = 255) String specialties
) {
}
