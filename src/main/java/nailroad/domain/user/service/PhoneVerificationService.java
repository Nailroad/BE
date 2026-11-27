package nailroad.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.user.code.PhoneErrorCode;
import nailroad.domain.user.entity.PhoneSendLog;
import nailroad.domain.user.entity.PhoneVerification;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.exception.PhoneException;
import nailroad.domain.user.repository.PhoneSendLogRepository;
import nailroad.domain.user.repository.PhoneVerificationRepository;
import nailroad.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long DAILY_WINDOW_SECONDS = 24 * 60 * 60;
	private final PhoneVerificationRepository repository;
	private final PhoneSendLogRepository sendLogRepository;
	private final UserRepository userRepository;
	private final PhoneCodeSender sender;

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.auth.phone.daily-limit-per-phone:5}")
	private int dailyLimitPerPhone;

	@Value("${app.auth.phone.daily-limit-per-user:10}")
	private int dailyLimitPerUser;

	@Transactional
	public void send(Long userId, String rawPhone) {
		String phone = normalize(rawPhone);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
		if (user.getOnboardingCompletedAt() != null) {
			throw new AuthException(AuthErrorCode.ONBOARDING_ALREADY_COMPLETE);
		}
		Instant now = Instant.now();
		PhoneVerification verification = repository.findByUserIdForUpdate(user.getId()).orElse(null);
		if (verification != null) {
			checkLock(verification, now);
			if (verification.getSentAt().plusSeconds(60).isAfter(now)) {
				throw new PhoneException(PhoneErrorCode.SEND_TOO_FREQUENT);
			}
		}
		checkDailyLimit(user.getId(), phone, now);
		String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
		String hash = hash(user.getId(), phone, code);
		if (verification == null) {
			verification = new PhoneVerification(user, phone, hash, now, now.plusSeconds(300));
		} else {
			verification.issue(phone, hash, now, now.plusSeconds(300));
		}
		repository.saveAndFlush(verification);
		sendLogRepository.save(new PhoneSendLog(user.getId(), phone, now));
		// 발송에 실패하면 예외로 트랜잭션이 롤백되어 인증번호와 발송 이력이 남지 않는다.
		sender.send(phone, code);
	}

	/** 문자 비용 악용을 막기 위해 최근 24시간 발송 횟수를 번호별·회원별로 제한한다. */
	private void checkDailyLimit(Long userId, String phone, Instant now) {
		Instant since = now.minusSeconds(DAILY_WINDOW_SECONDS);
		if (sendLogRepository.countByPhoneAndSentAtAfter(phone, since) >= dailyLimitPerPhone
				|| sendLogRepository.countByUserIdAndSentAtAfter(userId, since) >= dailyLimitPerUser) {
			throw new PhoneException(PhoneErrorCode.DAILY_LIMIT_EXCEEDED);
		}
	}

	@Transactional(noRollbackFor = PhoneException.class)
	public void verify(Long userId, String rawPhone, String code) {
		String phone = normalize(rawPhone);
		PhoneVerification verification = repository.findByUserIdForUpdate(userId)
			.orElseThrow(() -> new PhoneException(PhoneErrorCode.CODE_INVALID));
		Instant now = Instant.now();
		checkLock(verification, now);
		if (verification.getVerifiedAt() != null) {
			throw new PhoneException(PhoneErrorCode.CODE_INVALID);
		}
		if (!verification.getPhone().equals(phone)) {
			throw new PhoneException(PhoneErrorCode.CODE_INVALID);
		}
		if (!verification.getExpiresAt().isAfter(now)) {
			throw new PhoneException(PhoneErrorCode.CODE_EXPIRED);
		}
		byte[] expected = HexFormat.of().parseHex(verification.getCodeHash());
		byte[] actual = HexFormat.of().parseHex(hash(userId, phone, code));
		if (!MessageDigest.isEqual(expected, actual)) {
			verification.fail(now);
			throw new PhoneException(verification.getLockedUntil() == null
				? PhoneErrorCode.CODE_INVALID : PhoneErrorCode.TOO_MANY_ATTEMPTS);
		}
		verification.markVerified(now);
	}

	@Transactional
	public String consumeVerified(Long userId, String rawPhone) {
		String phone = normalize(rawPhone);
		PhoneVerification verification = repository.findByUserIdForUpdate(userId)
			.orElseThrow(() -> new PhoneException(PhoneErrorCode.NOT_VERIFIED));
		Instant verifiedAt = verification.getVerifiedAt();
		if (!verification.getPhone().equals(phone) || verifiedAt == null
				|| verifiedAt.plusSeconds(600).isBefore(Instant.now())) {
			throw new PhoneException(PhoneErrorCode.NOT_VERIFIED);
		}
		repository.delete(verification);
		return phone;
	}

	public String normalize(String rawPhone) {
		if (rawPhone == null || !rawPhone.matches("01[016789]-?\\d{3,4}-?\\d{4}")) {
			throw new PhoneException(PhoneErrorCode.INVALID_PHONE);
		}
		return rawPhone.replace("-", "");
	}

	private void checkLock(PhoneVerification verification, Instant now) {
		if (verification.getLockedUntil() == null) {
			return;
		}
		if (verification.getLockedUntil().isAfter(now)) {
			throw new PhoneException(PhoneErrorCode.TOO_MANY_ATTEMPTS);
		}
		verification.clearExpiredLock();
	}

	private String hash(Long userId, String phone, String code) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			String message = "nailroad-phone-v1:" + userId + ":" + phone + ":" + code;
			return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new IllegalStateException("Phone code hashing is unavailable", exception);
		}
	}
}
