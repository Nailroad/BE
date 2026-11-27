package nailroad.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "phone_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(name = "phone", nullable = false, length = 11)
	private String phone;

	@Column(name = "code_hash", nullable = false, length = 64)
	private String codeHash;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "verified_at")
	private Instant verifiedAt;

	@Column(name = "failed_attempts", nullable = false)
	private int failedAttempts;

	@Column(name = "locked_until")
	private Instant lockedUntil;

	public PhoneVerification(User user, String phone, String codeHash, Instant sentAt, Instant expiresAt) {
		this.user = user;
		issue(phone, codeHash, sentAt, expiresAt);
	}

	public void issue(String phone, String codeHash, Instant sentAt, Instant expiresAt) {
		this.phone = phone;
		this.codeHash = codeHash;
		this.sentAt = sentAt;
		this.expiresAt = expiresAt;
		this.verifiedAt = null;
	}

	public void markVerified(Instant now) {
		this.verifiedAt = now;
		this.codeHash = "verified";
		this.failedAttempts = 0;
		this.lockedUntil = null;
	}

	public void fail(Instant now) {
		this.failedAttempts++;
		if (failedAttempts >= 5) {
			this.lockedUntil = now.plusSeconds(15 * 60);
		}
	}

	public void clearExpiredLock() {
		this.failedAttempts = 0;
		this.lockedUntil = null;
	}
}
