package nailroad.domain.estimate.entity;

import jakarta.persistence.AttributeOverride;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.estimate.enums.EstimateRequestType;
import nailroad.domain.estimate.enums.RemovalType;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseCreatedEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "estimate")
@AttributeOverride(name = "createdAt", column = @Column(name = "create_at", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estimate extends BaseCreatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "estimate_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "estimate_image_url", nullable = false)
	private String estimateImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_type", nullable = false)
	private EstimateRequestType requestType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "preferred_slots", nullable = false)
	private List<Map<String, Object>> preferredSlots;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "extension_required")
	private Boolean extensionRequired;

	@Enumerated(EnumType.STRING)
	@Column(name = "removal_type")
	private RemovalType removalType;

	@Column(name = "customer_request")
	private String customerRequest;

	@Column(name = "select_location")
	private String selectedLocation;

	@Builder
	public Estimate(
		User user,
		String estimateImageUrl,
		EstimateRequestType requestType,
		List<Map<String, Object>> preferredSlots,
		LocalDateTime expiresAt,
		BigDecimal longitude,
		BigDecimal latitude,
		Boolean extensionRequired,
		RemovalType removalType,
		String customerRequest,
		String selectedLocation
	) {
		this.user = user;
		this.estimateImageUrl = estimateImageUrl;
		this.requestType = requestType;
		this.preferredSlots = preferredSlots;
		this.expiresAt = expiresAt;
		this.longitude = longitude;
		this.latitude = latitude;
		this.extensionRequired = extensionRequired;
		this.removalType = removalType;
		this.customerRequest = customerRequest;
		this.selectedLocation = selectedLocation;
	}
}
