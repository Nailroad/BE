package nailroad.global.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class SecurityConfigOAuth2LoginTest {

	private final DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
		new InMemoryClientRegistrationRepository(kakao()), OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

	@Test
	void startsKakaoLoginAtSpringDefaultUri() {
		OAuth2AuthorizationRequest request = resolver.resolve(new MockHttpServletRequest("GET", "/oauth2/authorization/kakao"));

		assertNotNull(request);
		assertTrue(request.getAuthorizationRequestUri().startsWith("https://kauth.kakao.com/oauth/authorize"));
	}

	@Test
	void doesNotTreatOtherAuthPathsAsLogin() {
		assertNull(resolver.resolve(new MockHttpServletRequest("GET", "api/v1/auth/me")));
		assertNull(resolver.resolve(new MockHttpServletRequest("GET", "api/v1/auth/csrf")));
		assertNull(resolver.resolve(new MockHttpServletRequest("GET", "api/v1/auth/login-complete")));
	}

	private static ClientRegistration kakao() {
		return ClientRegistration.withRegistrationId("kakao")
			.clientId("test-client")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("http://localhost:8080/auth/kakao/callback")
			.authorizationUri("https://kauth.kakao.com/oauth/authorize")
			.tokenUri("https://kauth.kakao.com/oauth/token")
			.build();
	}
}
