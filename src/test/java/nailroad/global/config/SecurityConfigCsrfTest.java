package nailroad.global.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigCsrfTest {

	@Test
	void protectsOnlyCookieAuthenticatedEndpoints() {
		assertTrue(matches("POST", "/api/v1/auth/reissue"));
		assertTrue(matches("POST", "/api/v1/auth/logout"));
	}

	@Test
	void skipsBearerAuthenticatedEndpoints() {
		assertFalse(matches("POST", "/api/v1/onboarding/phone/send"));
		assertFalse(matches("POST", "/api/v1/onboarding"));
		assertFalse(matches("POST", "/api/v1/designs"));
		assertFalse(matches("GET", "/api/v1/auth/reissue"));
	}

	private boolean matches(String method, String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		return SecurityConfig.CSRF_PROTECTED.matches(request);
	}
}
