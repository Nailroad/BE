package nailroad.domain.user.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.user.enums.PreferredStyleCode;
import nailroad.domain.user.enums.AccountType;
import nailroad.domain.user.enums.AuthProvider;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "auth_provider", nullable = false)
	private AuthProvider authProvider;

	@Column(name = "name")
	private String name;

	@Column(name = "phone")
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type")
	private AccountType accountType;

	@Column(name = "onboarding_completed_at")
	private LocalDateTime onboardingCompletedAt;

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

	@Column(name = "push_enabled", nullable = false)
	private boolean pushEnabled;

	@Builder
	public User(
		String providerUserId,
		AuthProvider authProvider,
		String name,
		String phone,
		AccountType accountType,
		BigDecimal longitude,
		BigDecimal latitude,
		String email,
		PreferredStyleCode preferredStyleCodes,
		boolean pushEnabled
	) {
		this.providerUserId = providerUserId;
		this.authProvider = authProvider;
		this.name = name;
		this.phone = phone;
		this.accountType = accountType;
		this.longitude = longitude;
		this.latitude = latitude;
		this.email = email;
		this.preferredStyleCodes = preferredStyleCodes;
		this.pushEnabled = pushEnabled;
	}

	public void changePushEnabled(boolean pushEnabled) {
		this.pushEnabled = pushEnabled;
	}

	/** 회원가입 마지막 단계에서 유형·전화번호를 한 번에 확정한다. 이후에는 변경할 수 없다. */
	/** 응답(onboardingComplete)과 권한(ROLE_ONBOARDING) 판단에 같은 기준을 쓴다. */
	public boolean isOnboardingComplete() {
		return onboardingCompletedAt != null && accountType != null;
	}

	public void completeOnboarding(AccountType accountType, String phone, LocalDateTime completedAt) {
		if (accountType == null || onboardingCompletedAt != null) {
			throw new IllegalStateException("Onboarding cannot be completed");
		}
		this.accountType = accountType;
		this.phone = phone;
		this.onboardingCompletedAt = completedAt;
	}
}
