package nailroad.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PushSettingRequest(
	@NotNull @Schema(description = "푸시 알림 수신 여부", example = "true") Boolean pushEnabled
) {
}
