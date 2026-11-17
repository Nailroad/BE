package nailroad.domain.estimate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import nailroad.domain.estimate.enums.EstimateResponseStatus;
import nailroad.domain.shop.entity.Shop;

@Getter
@Entity
@Table(
	name = "estimate_response",
	uniqueConstraints = @UniqueConstraint(columnNames = {"estimate_id", "shop_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "estimate_response_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estimate_id", nullable = false)
	private Estimate estimate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EstimateResponseStatus status;

	@Column(name = "price")
	private Integer price;

	@Column(name = "responsed_at")
	private LocalDateTime respondedAt;

	@Column(name = "shop_note")
	private String shopNote;

	@Column(name = "duration_times")
	private Integer durationTimes;

	@Builder
	public EstimateResponse(Estimate estimate, Shop shop, EstimateResponseStatus status) {
		this.estimate = estimate;
		this.shop = shop;
		this.status = status;
	}
}
