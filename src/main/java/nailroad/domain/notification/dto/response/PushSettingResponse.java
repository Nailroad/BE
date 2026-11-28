package nailroad.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import nailroad.domain.user.entity.User;

public record PushSettingResponse(
	@Schema(description = "푸시 알림 수신 여부", example = "true") boolean pushEnabled
) {

	public static PushSettingResponse from(User user) {
		return new PushSettingResponse(user.isPushEnabled());
	}
}
