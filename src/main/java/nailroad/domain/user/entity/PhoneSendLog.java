package nailroad.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 인증번호 발송 이력. 24시간 발송 횟수 제한에만 쓴다. */
@Getter
@Entity
@Table(name = "phone_send_log", indexes = {
	@Index(name = "idx_phone_send_log_phone_sent_at", columnList = "phone, sent_at"),
	@Index(name = "idx_phone_send_log_user_sent_at", columnList = "user_id, sent_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneSendLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "phone", nullable = false, length = 11)
	private String phone;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	public PhoneSendLog(Long userId, String phone, Instant sentAt) {
		this.userId = userId;
		this.phone = phone;
		this.sentAt = sentAt;
	}
}
