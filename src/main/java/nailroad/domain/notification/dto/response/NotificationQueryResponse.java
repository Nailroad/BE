package nailroad.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import nailroad.domain.notification.entity.Notification;
import nailroad.domain.notification.enums.NotificationType;
import org.springframework.data.domain.Slice;

public final class NotificationQueryResponse {

	private NotificationQueryResponse() {
	}

	@Schema(name = "NotificationListResponse")
	public record ListResponse(
		List<Item> items,
		int size,
		boolean hasNext,
		@Schema(description = "다음 페이지 요청의 cursor에 전달할 마지막 알림 ID") Long nextCursor
	) {

		public static ListResponse from(Slice<Notification> page) {
			List<Item> items = page.stream().map(Item::from).toList();
			Long nextCursor = page.hasNext() ? items.getLast().notificationId() : null;
			return new ListResponse(items, page.getSize(), page.hasNext(), nextCursor);
		}

		public record Item(
			Long notificationId,
			@Schema(description = "알림 종류. 알림을 눌렀을 때 이동할 화면을 정합니다.") NotificationType type,
			String title,
			String body,
			@Schema(description = "알림을 눌렀을 때 이동할 대상 ID (예약 ID, 견적 ID 등)") Long targetId,
			boolean isRead,
			LocalDateTime createdAt
		) {

			public static Item from(Notification notification) {
				return new Item(notification.getId(), notification.getType(), notification.getTitle(),
					notification.getBody(), notification.getTargetId(), notification.isRead(),
					notification.getCreatedAt());
			}
		}
	}

	@Schema(name = "UnreadNotificationResponse")
	public record UnreadResponse(
		@Schema(description = "읽지 않은 알림이 있는지 여부", example = "true") boolean hasUnread
	) {
	}
}
