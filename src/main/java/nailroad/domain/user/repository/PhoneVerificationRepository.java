package nailroad.domain.user.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import nailroad.domain.user.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select verification from PhoneVerification verification where verification.user.id = :userId")
	Optional<PhoneVerification> findByUserIdForUpdate(@Param("userId") Long userId);
}
