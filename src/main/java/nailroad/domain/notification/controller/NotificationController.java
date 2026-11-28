package nailroad.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nailroad.domain.notification.dto.request.PushSettingRequest;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.notification.service.PushSettingService;
import nailroad.domain.user.entity.User;
import nailroad.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final PushSettingService pushSettingService;

	@GetMapping("/push-setting")
	@Operation(summary = "푸시 알림 설정 조회", description = "로그인한 사용자의 푸시 알림 수신 여부를 조회합니다.")
	public ApiResponse<PushSettingResponse> getPushSetting(
		@Parameter(hidden = true) @AuthenticationPrincipal User user
	) {
		return ApiResponse.onSuccess("푸시 알림 설정을 조회했습니다.", PushSettingResponse.from(user));
	}

	@PatchMapping("/push-setting")
	@Operation(summary = "푸시 알림 설정 변경", description = "로그인한 사용자의 푸시 알림 수신 여부를 변경합니다.")
	public ApiResponse<PushSettingResponse> changePushSetting(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@Valid @RequestBody PushSettingRequest request
	) {
		PushSettingResponse response = pushSettingService.change(user.getId(), request.pushEnabled());
		return ApiResponse.onSuccess("푸시 알림 설정을 변경했습니다.", response);
	}
}
