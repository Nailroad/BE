package nailroad.domain.event.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.event.enums.EventType;

@Getter
@Entity
@Table(name = "curation_design")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type")
	private EventType eventType;

	@Column(name = "starts_at")
	private LocalDateTime startsAt;

	@Column(name = "ends_at")
	private LocalDateTime endsAt;

	@Column(name = "content")
	private String content;

	@Column(name = "event_image_url")
	private String eventImageUrl;

	@Column(name = "target_id")
	private Long targetId;

	@Builder
	public event(
		EventType eventType,
		LocalDateTime startsAt,
		LocalDateTime endsAt,
		String content,
		String eventImageUrl,
		Long targetId
	) {
		this.eventType = eventType;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.content = content;
		this.eventImageUrl = eventImageUrl;
		this.targetId = targetId;
	}
}
