package nailroad.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nailroad.domain.image.enums.ImagePurpose;

public record ImageUploadUrlRequest(
        @NotNull @Schema(description = "이미지 용도", example = "DESIGN") ImagePurpose purpose,
        @NotBlank @Schema(description = "업로드할 파일의 MIME 타입",
                allowableValues = {"image/jpeg", "image/png", "image/webp"}, example = "image/jpeg") String contentType
) {
}
