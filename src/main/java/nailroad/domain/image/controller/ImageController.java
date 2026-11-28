package nailroad.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nailroad.domain.image.code.ImageErrorCode;
import nailroad.domain.image.dto.ImageUploadUrlRequest;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
import nailroad.domain.image.enums.ImagePurpose;
import nailroad.domain.image.service.ImageUploadUrlService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            description = """
                    요청한 파일 순서대로 업로드 URL을 발급합니다. 각 URL에 해당 파일을 PUT으로 전송합니다.
                    SHOP은 승인된 샵의 소유자가 shopId를 전달해야 합니다.
                    ESTIMATE는 매칭(직접 업로드) 견적 요청 이미지이며, 로그인한 회원의 경로(estimates/{userId})로 발급합니다.
                    """)
    public ApiResponse<ImageUploadUrlResponse> issueUploadUrl(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody ImageUploadUrlRequest request
    ) {
        if (user == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        ImagePurpose purpose = parsePurpose(request.purpose());
        ImageUploadUrlResponse response = switch (purpose) {
            case SHOP -> imageUploadUrlService.issueForShop(user.getId(), request.shopId(), request.files());
            case ESTIMATE -> imageUploadUrlService.issueForEstimate(user.getId(), request.files());
            default -> imageUploadUrlService.issue(purpose, request.files());
        };
        return ApiResponse.onSuccess("이미지 업로드 URL을 발급했습니다.", response);
    }

    private ImagePurpose parsePurpose(String purpose) {
        try {
            return ImagePurpose.valueOf(purpose);
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ImageErrorCode.INVALID_PURPOSE);
        }
    }
}
