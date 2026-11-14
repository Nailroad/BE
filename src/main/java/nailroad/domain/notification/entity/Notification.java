package nailroad.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.notification.enums.NotificationType;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseCreatedEntity;

@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseCreatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "title")
	private String title;

	@Column(name = "body")
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private NotificationType type;

	@Column(name = "content")
	private String content;

	@Column(name = "is_read", nullable = false)
	private Boolean read;

	@Column(name = "target_id")
	private Long targetId;

	@Builder
	public Notification(
		User user,
		String title,
		String body,
		NotificationType type,
		String content,
		Boolean read,
		Long targetId
	) {
		this.user = user;
		this.title = title;
		this.body = body;
		this.type = type;
		this.content = content;
		this.read = read;
		this.targetId = targetId;
	}
}
