package nailroad.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUploadUrlResponse(
        @Schema(description = "이미지 파일을 PUT으로 전송할 S3 Presigned URL") String uploadUrl,
        @Schema(description = "S3 객체 키", example = "designs/550e8400-e29b-41d4-a716-446655440000.jpg") String imageKey,
        @Schema(description = "발급 시점부터 URL이 유효한 시간(초)", example = "300") int expiresInSeconds
) {
}
