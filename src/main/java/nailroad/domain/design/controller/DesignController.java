package nailroad.domain.design.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nailroad.domain.design.code.DesignErrorCode;
import nailroad.domain.design.dto.DesignDetailResponse;
import nailroad.domain.design.dto.DesignSearchResponse;
import nailroad.domain.design.service.DesignService;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/designs")
@RequiredArgsConstructor
@Tag(name = "Design", description = "디자인 검색 API")
public class DesignController {
    private final DesignService designService;

    //디자인 검색
    @GetMapping
    @Operation(summary = "디자인 목록 조회", description = "디자인 이름 또는 TagName enum 이름으로 검색하고 필터를 적용합니다.")
    public ApiResponse<DesignSearchResponse> searchDesigns(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categoryCode,
            @RequestParam(required = false) List<String> shapeCode,
            @RequestParam(required = false) List<String> colorCode,
            @RequestParam(required = false) List<String> tagName,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") String size
    ) {
        return ApiResponse.onSuccess("디자인 목록을 조회했습니다.",
                designService.search(userId, keyword, categoryCode, shapeCode, colorCode, tagName, cursor, size));
    }

    @GetMapping("/{designId}")
    @Operation(summary = "디자인 상세 조회", description = "디자인의 태그와 로그인 사용자의 좋아요 여부를 함께 조회합니다.")
    public ApiResponse<DesignDetailResponse> getDesign(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String designId
    ) {
        Long parsedDesignId;
        try {
            parsedDesignId = Long.parseLong(designId);
        } catch (NumberFormatException exception) {
            throw new CustomException(DesignErrorCode.INVALID_DESIGN_ID);
        }
        return ApiResponse.onSuccess("디자인 상세 정보를 조회했습니다.",
                designService.getDetail(userId, parsedDesignId));
    }
}


