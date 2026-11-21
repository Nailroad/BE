package nailroad.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import nailroad.domain.image.dto.ImageUploadUrlRequest;
import nailroad.domain.image.dto.ImageUploadUrlResponse;
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
            description = "로그인한 사용자가 업로드 URL을 발급받습니다. "
                    + "응답의 uploadUrl에 발급 요청과 같은 Content-Type 헤더를 사용해 이미지 파일을 PUT으로 전송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발급 성공",
                    useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 이미지 용도·타입",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서명 URL 발급 중 서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ImageUploadUrlResponse> issueUploadUrl(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody ImageUploadUrlRequest request
    ) {
        if (principal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.onSuccess("이미지 업로드 URL을 발급했습니다.", imageUploadUrlService.issue(request));
    }
}
