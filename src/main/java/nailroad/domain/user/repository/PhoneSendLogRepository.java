package nailroad.domain.user.repository;

import java.time.Instant;
import nailroad.domain.user.entity.PhoneSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneSendLogRepository extends JpaRepository<PhoneSendLog, Long> {

	long countByPhoneAndSentAtAfter(String phone, Instant since);

	long countByUserIdAndSentAtAfter(Long userId, Instant since);
}
