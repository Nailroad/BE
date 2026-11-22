package nailroad.domain.image.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import nailroad.domain.image.enums.ImagePurpose;

public record ImageUploadUrlRequest(
        @NotBlank @Schema(implementation = ImagePurpose.class, example = "SHOP") String purpose,
        @NotEmpty @ArraySchema(minItems = 1, arraySchema = @Schema(description = "업로드할 이미지 파일 목록"))
        List<@NotNull @Valid FileMetadata> files
) {

    public record FileMetadata(
            @NotBlank @Schema(description = "이미지 파일의 MIME 타입", example = "image/jpeg",
                    allowableValues = {"image/jpeg", "image/png", "image/webp", "image/heic", "image/heif"})
            String contentType,
            @NotNull @Schema(description = "이미지 파일 크기(바이트), 1바이트 이상 5MB 이하", example = "248530")
            Long sizeBytes
    ) {
    }
}
