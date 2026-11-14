package nailroad.domain.terms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.user.entity.User;

@Getter
@Entity
@Table(name = "account_terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountTerms {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_terms_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "terms_id", nullable = false)
	private Terms terms;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "is_agreed", nullable = false)
	private Boolean agreed;

	@Column(name = "recorded_at", nullable = false)
	private LocalDateTime recordedAt;

	@Builder
	public AccountTerms(Terms terms, User user, Boolean agreed, LocalDateTime recordedAt) {
		this.terms = terms;
		this.user = user;
		this.agreed = agreed;
		this.recordedAt = recordedAt;
	}
}
