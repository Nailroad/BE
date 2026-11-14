package nailroad.domain.estimate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.shop.entity.Designer;

@Getter
@Entity
@Table(
	name = "estimate_schedule",
	uniqueConstraints = @UniqueConstraint(columnNames = {"estimate_response_id", "starts_at"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "estimate_schedule_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estimate_response_id", nullable = false)
	private EstimateResponse estimateResponse;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "designer_id", nullable = false)
	private Designer designer;

	@Column(name = "starts_at", nullable = false)
	private LocalDateTime startsAt;

	@Column(name = "ends_at", nullable = false)
	private LocalDateTime endsAt;

	@Builder
	public EstimateSchedule(
		EstimateResponse estimateResponse,
		Designer designer,
		LocalDateTime startsAt,
		LocalDateTime endsAt
	) {
		this.estimateResponse = estimateResponse;
		this.designer = designer;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
	}
}
