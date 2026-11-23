package nailroad.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtServiceTest {

	private final JwtService jwtService = new JwtService("0123456789abcdef0123456789abcdef");

	@Test
	void issuedTokenContainsUserId() {
		String token = jwtService.issue(42L);
		assertEquals("42", jwtService.decode(token).getSubject());
	}

	@Test
	void rejectsTamperedToken() {
		String token = jwtService.issue(42L);
		String[] parts = token.split("\\.");
		String signature = parts[2];
		String tampered = parts[0] + "." + parts[1] + "."
			+ (signature.startsWith("A") ? "B" : "A") + signature.substring(1);
		assertThrows(JwtException.class, () -> jwtService.decode(tampered));
	}
}
