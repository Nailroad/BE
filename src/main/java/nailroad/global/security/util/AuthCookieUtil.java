package nailroad.global.security.util;

import java.time.Duration;
import nailroad.domain.user.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieUtil {

	public static final String REFRESH_COOKIE = "REFRESH_TOKEN";

	private final boolean secure;
	private final String sameSite;

	public AuthCookieUtil(@Value("${app.auth.cookie-secure}") boolean secure,
			@Value("${app.auth.cookie-same-site}") String sameSite) {
		this.secure = secure;
		this.sameSite = sameSite;
	}

	public ResponseCookie refreshToken(String token) {
		return cookie(token, RefreshTokenService.REFRESH_TOKEN_TTL);
	}

	public ResponseCookie clearRefreshToken() {
		return cookie("", Duration.ZERO);
	}

	private ResponseCookie cookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
			.httpOnly(true)
			.secure(secure)
			.sameSite(sameSite)
				.path("/api/v1/auth")
			.maxAge(maxAge)
			.build();
	}
}
