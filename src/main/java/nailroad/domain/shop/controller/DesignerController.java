package nailroad.domain.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nailroad.domain.shop.dto.request.DesignerRegistrationRequest;
import nailroad.domain.shop.dto.request.DesignerUpdateRequest;
import nailroad.domain.shop.dto.response.DesignerResponse;
import nailroad.domain.shop.service.DesignerService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops/{shopId}/designers")
@RequiredArgsConstructor
@Tag(name = "Designer", description = "샵 디자이너 API")
public class DesignerController {

	private final DesignerService designerService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "디자이너 등록", description = "샵 소유자가 본인 샵에 디자이너를 등록합니다.")
	public ApiResponse<DesignerResponse> register(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long shopId,
		@Valid @RequestBody DesignerRegistrationRequest request
	) {
		if (user == null) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}
		return ApiResponse.onCreated(designerService.register(user.getId(), shopId, request));
	}

	@PostMapping("/{designerId}")
	@Operation(summary = "디자이너 정보 수정", description = "샵 소유자가 본인 샵 디자이너의 프로필에서 값을 전달한 항목을 수정합니다.")
	public ApiResponse<DesignerResponse> update(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long shopId,
		@PathVariable Long designerId,
		@Valid @RequestBody DesignerUpdateRequest request
	) {
		if (user == null) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}
		return ApiResponse.onSuccess("디자이너 정보를 수정했습니다.",
			designerService.update(user.getId(), shopId, designerId, request));
	}
}
