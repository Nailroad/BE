package nailroad.domain.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nailroad.domain.shop.service.ShopLikeService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops/{shopId}/like")
@Tag(name = "Shop Like", description = "샵 찜 API")
public class ShopLikeController {

	private final ShopLikeService shopLikeService;

	@PutMapping
	@Operation(summary = "샵 찜 추가", description = "승인된 샵을 찜합니다. 이미 찜한 샵이면 그대로 성공합니다.")
	public ApiResponse<Void> like(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@PathVariable Long shopId
	) {
		if (user == null) {
			throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
		}
		shopLikeService.like(user.getId(), shopId);
		return ApiResponse.onSuccess("샵을 찜했습니다.");
	}
}
