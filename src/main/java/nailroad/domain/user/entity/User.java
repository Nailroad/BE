package nailroad.domain.user.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.user.enums.PreferredStyleCode;
import nailroad.global.common.BaseEntity;

@Getter
@Entity
@Table(
	name = "\"user\"",
	uniqueConstraints = @UniqueConstraint(columnNames = {"auth_provider", "provider_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "provider_user_id", nullable = false)
	private String providerUserId;

	@Column(name = "auth_provider", nullable = false)
	private String authProvider;

	@Column(name = "name")
	private String name;

	@Column(name = "phone")
	private String phone;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "email")
	private String email;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_style_codes")
	private PreferredStyleCode preferredStyleCodes;

	@Column(name = "push_enabled")
	private Boolean pushEnabled;

	@Builder
	public User(
		String providerUserId,
		String authProvider,
		String name,
		String phone,
		BigDecimal longitude,
		BigDecimal latitude,
		String email,
		PreferredStyleCode preferredStyleCodes,
		Boolean pushEnabled
	) {
		this.providerUserId = providerUserId;
		this.authProvider = authProvider;
		this.name = name;
		this.phone = phone;
		this.longitude = longitude;
		this.latitude = latitude;
		this.email = email;
		this.preferredStyleCodes = preferredStyleCodes;
		this.pushEnabled = pushEnabled;
	}
}
