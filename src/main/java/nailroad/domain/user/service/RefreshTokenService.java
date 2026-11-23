package nailroad.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import nailroad.domain.user.code.AuthErrorCode;
import nailroad.domain.user.entity.RefreshToken;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.exception.AuthException;
import nailroad.domain.user.repository.RefreshTokenRepository;
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
		RefreshToken stored = findValid(oldValue);
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
		if (value == null || value.isBlank()) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		RefreshToken stored = repository.findByTokenHash(hash(value))
			.orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
		return requireValid(stored).getUser();
	}

	private RefreshToken findValid(String value) {
		if (value == null || value.isBlank()) {
			throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		RefreshToken stored = repository.findByTokenHashForUpdate(hash(value))
			.orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
		return requireValid(stored);
	}

	private RefreshToken requireValid(RefreshToken stored) {
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
