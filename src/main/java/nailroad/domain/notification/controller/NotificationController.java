package nailroad.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nailroad.domain.notification.dto.request.NotificationReadRequest;
import nailroad.domain.notification.dto.request.PushSettingRequest;
import nailroad.domain.notification.dto.response.NotificationQueryResponse.ListResponse;
import nailroad.domain.notification.dto.response.NotificationQueryResponse.UnreadResponse;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.notification.service.NotificationService;
import nailroad.domain.notification.service.PushSettingService;
import nailroad.domain.user.entity.User;
import nailroad.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final NotificationService notificationService;
	private final PushSettingService pushSettingService;

	@GetMapping
	@Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림을 최신순으로 조회합니다.")
	public ApiResponse<ListResponse> getList(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@Parameter(description = "이전 응답의 nextCursor에 담긴 마지막 알림 ID") @RequestParam(required = false) Long cursor,
		@Parameter(description = "한 번에 조회할 알림 수 (기본 20, 최대 100)") @RequestParam(required = false) Integer size
	) {
		return ApiResponse.onSuccess("알림 목록을 조회했습니다.", notificationService.getList(user.getId(), cursor, size));
	}

	@GetMapping("/unread")
	@Operation(summary = "안 읽은 알림 여부 조회", description = "읽지 않은 알림이 있는지 조회합니다. 홈 화면 알림 표시에 사용합니다.")
	public ApiResponse<UnreadResponse> getUnread(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		return ApiResponse.onSuccess("안 읽은 알림 여부를 조회했습니다.", notificationService.getUnread(user.getId()));
	}

	@PatchMapping("/read")
	@Operation(summary = "알림 읽음 처리",
		description = "선택한 알림을 읽음 처리합니다. 알림 하나를 누를 때도 ID 하나를 담아 호출합니다. "
			+ "이미 읽은 알림이 섞여 있어도 성공하고, 없는 알림이나 다른 사용자의 알림이 섞여 있으면 어떤 알림도 읽음 처리하지 않습니다.")
	public ApiResponse<Void> read(
		@Parameter(hidden = true) @AuthenticationPrincipal User user,
		@Valid @RequestBody NotificationReadRequest request
	) {
		notificationService.read(user.getId(), request.notificationIds());
		return ApiResponse.onSuccess("알림을 읽음 처리했습니다.");
	}

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
