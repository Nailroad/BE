package nailroad.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NotificationReadRequest(
	@NotEmpty @Size(max = 100)
	@ArraySchema(arraySchema = @Schema(description = "읽음 처리할 알림 ID 목록 (1개 이상 100개 이하)"),
		schema = @Schema(example = "5"))
	List<@NotNull Long> notificationIds
) {
}
