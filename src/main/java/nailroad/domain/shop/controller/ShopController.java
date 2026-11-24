package nailroad.domain.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nailroad.domain.shop.dto.request.ShopQueryRequest;
import nailroad.domain.shop.dto.request.ShopRegistrationRequest;
import nailroad.domain.shop.dto.request.ShopUpdateRequest;
import nailroad.domain.shop.dto.response.ShopQueryResponse;
import nailroad.domain.shop.dto.response.ShopRegistrationResponse;
import nailroad.domain.shop.dto.response.ShopUpdateResponse;
import nailroad.domain.shop.service.ShopQueryService;
import nailroad.domain.shop.service.ShopService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "샵 API")
public class ShopController {

	private final ShopService shopService;
	private final ShopQueryService shopQueryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "샵 등록 신청", description = "샵 기본정보를 제출하면 PENDING 상태로 접수합니다.")
	public ApiResponse<ShopRegistrationResponse> register(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@Valid @RequestBody ShopRegistrationRequest request
	) {
		if (user == null) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}
		return ApiResponse.onCreated(shopService.register(user.getId(), request));
	}

	@PatchMapping("/{shopId}")
	@Operation(summary = "승인된 샵 기본정보 수정", description = "승인된 샵의 소유자가 기본정보를 수정합니다.")
	public ApiResponse<ShopUpdateResponse> update(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long shopId,
		@Valid @RequestBody ShopUpdateRequest request
	) {
		if (user == null) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}
		return ApiResponse.onSuccess("샵 기본정보를 수정했습니다.", shopService.update(user.getId(), shopId, request));
	}

	@GetMapping
	@Operation(summary = "샵 목록 조회", description = "공개된 샵을 이름으로 검색합니다. 지도 범위가 있으면 거리, 최신, 별점, 리뷰 수로 정렬할 수 있습니다.")
	public ApiResponse<ShopQueryResponse.ListResponse> getShops(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@ParameterObject @ModelAttribute ShopQueryRequest request,
		BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			throw new CustomException(GlobalErrorCode.BAD_REQUEST);
		}
		Long userId = user == null ? null : user.getId();
		return ApiResponse.onSuccess("샵 목록을 조회했습니다.", shopQueryService.getList(userId, request));
	}

	@GetMapping("/{shopId}")
	@Operation(summary = "샵 상세 조회", description = "공개된 샵의 기본정보와 가격표를 조회합니다.")
	public ApiResponse<ShopQueryResponse.DetailResponse> getShop(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long shopId
	) {
		Long userId = user == null ? null : user.getId();
		return ApiResponse.onSuccess("샵 상세 정보를 조회했습니다.", shopQueryService.getDetail(userId, shopId));
	}
}
