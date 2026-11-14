package nailroad.domain.reservation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nailroad.domain.estimate.entity.EstimateSchedule;
import nailroad.domain.reservation.enums.ReservationStatus;
import nailroad.domain.user.entity.User;
import nailroad.global.common.BaseEntity;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estiamate_schedule_id", nullable = false)
	private EstimateSchedule estimateSchedule;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ReservationStatus status;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "cancel_reason")
	private String cancelReason;

	@Builder
	public Reservation(User user, EstimateSchedule estimateSchedule, ReservationStatus status) {
		this.user = user;
		this.estimateSchedule = estimateSchedule;
		this.status = status;
	}
}
