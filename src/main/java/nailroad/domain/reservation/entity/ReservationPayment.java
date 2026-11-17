package nailroad.domain.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reservation_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationPayment {

	@Id
	@Column(name = "reservation_id")
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;

	@Column(name = "deposit_price", nullable = false)
	private Integer depositPrice;

	@Column(name = "deposit_due_at", nullable = false)
	private LocalDateTime depositDueAt;

	@Column(name = "deposited_at")
	private LocalDateTime depositedAt;

	@Column(name = "deposit_verified_at")
	private LocalDateTime depositVerifiedAt;

	@Column(name = "fee_price")
	private Integer feePrice;

	@Column(name = "payment_price")
	private Integer paymentPrice;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Builder
	public ReservationPayment(Reservation reservation, Integer depositPrice, LocalDateTime depositDueAt) {
		this.reservation = reservation;
		this.depositPrice = depositPrice;
		this.depositDueAt = depositDueAt;
	}
}
