package nailroad.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import nailroad.domain.image.code.ImageErrorCode;
import nailroad.domain.image.dto.ImageUploadUrlRequest;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
import nailroad.domain.image.enums.ImagePurpose;
import nailroad.domain.image.service.ImageUploadUrlService;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 업로드 API")
public class ImageController {

    private final ImageUploadUrlService imageUploadUrlService;

    @PostMapping(path = "/presigned-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "이미지 업로드 URL 발급",
            description = "요청한 파일 순서대로 업로드 URL을 발급합니다. 각 URL에 해당 파일을 PUT으로 전송합니다.")
    public ApiResponse<ImageUploadUrlResponse> issueUploadUrl(
            // TODO: 소셜 로그인 연동 후 @AuthenticationPrincipal CustomUserDetails principal로 교체
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody ImageUploadUrlRequest request
    ) {
        if (principal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.onSuccess("이미지 업로드 URL을 발급했습니다.", imageUploadUrlService.issue(
                parsePurpose(request.purpose()), request.files()));
    }

    private ImagePurpose parsePurpose(String purpose) {
        try {
            return ImagePurpose.valueOf(purpose);
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ImageErrorCode.INVALID_PURPOSE);
        }
    }
}
