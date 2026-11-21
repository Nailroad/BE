package nailroad.domain.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nailroad.domain.shop.dto.request.ShopRegistrationRequest;
import nailroad.domain.shop.dto.request.ShopUpdateRequest;
import nailroad.domain.shop.dto.response.ShopRegistrationResponse;
import nailroad.domain.shop.dto.response.ShopUpdateResponse;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@Tag(name = "Shop", description = "샵 API")
public class ShopController {

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "샵 등록 신청", description = "샵 기본정보를 제출하면 PENDING 상태로 접수합니다.")
	public ApiResponse<ShopRegistrationResponse> register(
		// TODO: JWT 연동 시 추가: @AuthenticationPrincipal CustomUserDetails principal,
		@Valid @RequestBody ShopRegistrationRequest request
	) {
		// JWT 연동 후 shopService.register(principal.getUserId(), request)를 호출합니다.
		throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
	}

	@PatchMapping("/{shopId}")
	@Operation(summary = "승인된 샵 기본정보 수정", description = "승인된 샵의 소유자가 기본정보를 수정합니다.")
	public ApiResponse<ShopUpdateResponse> update(
		// TODO: JWT 연동 시 추가: @AuthenticationPrincipal CustomUserDetails principal,
		@PathVariable Long shopId,
		@Valid @RequestBody ShopUpdateRequest request
	) {
		// JWT 연동 후 shopService.update(principal.getUserId(), shopId, request)를 호출합니다.
		throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
	}
}
