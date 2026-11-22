package nailroad.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ImageUploadUrlResponse(
        @Schema(description = "요청한 파일 순서대로 발급된 업로드 정보") List<UploadItem> items
) {

    public record UploadItem(
            @Schema(description = "이미지 파일을 직접 PUT할 S3 Presigned URL") String uploadUrl,
            @Schema(description = "S3 객체 키", example = "shops/550e8400-e29b-41d4-a716-446655440000.jpg")
            String imageKey,
            @Schema(description = "발급 시점부터 URL이 유효한 시간(초)", example = "300") int expiresInSeconds
    ) {
    }
}
