package nailroad.domain.design.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nailroad.domain.design.dto.DesignSearchResponse;
import nailroad.domain.design.service.DesignService;
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
}


