package nailroad.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.entity.RefreshToken;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.auth.repository.RefreshTokenRepository;
import nailroad.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final SecureRandom RANDOM = new SecureRandom();
	public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);
	private final RefreshTokenRepository repository;

	@Transactional
	public String issue(User user) {
		String value = randomToken();
		repository.save(new RefreshToken(user, hash(value), Instant.now().plus(REFRESH_TOKEN_TTL)));
		return value;
	}

	@Transactional
	public RotatedToken rotate(String oldValue) {
		RefreshToken stored = requireValid(repository.findByTokenHashForUpdate(requireTokenHash(oldValue)));
		String nextValue = randomToken();
		stored.rotate(hash(nextValue), Instant.now().plus(REFRESH_TOKEN_TTL));
		return new RotatedToken(stored.getUser(), nextValue);
	}

	@Transactional
	public void revoke(String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		repository.findByTokenHashForUpdate(hash(value)).ifPresent(repository::delete);
	}

	@Transactional(readOnly = true)
	public User currentUser(String value) {
		return requireValid(repository.findByTokenHash(requireTokenHash(value))).getUser();
	}

	/** 빈 값이면 거절하고, DB에 저장된 형식(해시)으로 바꾼다. */
	private String requireTokenHash(String value) {
		if (value == null || value.isBlank()) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		return hash(value);
	}

	/** 조회 결과의 존재·만료·탈퇴 여부를 한곳에서 검사한다. */
	private RefreshToken requireValid(Optional<RefreshToken> found) {
		RefreshToken stored = found.orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
		if (!stored.getExpiresAt().isAfter(Instant.now()) || stored.getUser().getDeletedAt() != null) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		return stored;
	}

	private String randomToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	public record RotatedToken(User user, String value) {
	}
}
