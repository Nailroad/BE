package nailroad.global.security.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.auth.service.KakaoUserService;
import nailroad.domain.auth.service.RefreshTokenService;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.global.security.util.AuthCookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

class KakaoLoginSuccessHandlerTest {

	private static final String FAILURE_URL = "http://localhost:5173/login";

	private final KakaoUserService kakaoUserService = mock(KakaoUserService.class);
	private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
	private final LoginFailureRedirector redirector = new LoginFailureRedirector(FAILURE_URL);
	private final KakaoLoginSuccessHandler handler = new KakaoLoginSuccessHandler(kakaoUserService,
		refreshTokenService, redirector, new AuthCookieUtil(false, "Lax"));
	private final DefaultOAuth2User kakaoUser = new DefaultOAuth2User(List.of(), Map.of("id", 123L), "id");

	@Test
	void callbackSetsOnlyRefreshCookie() throws Exception {
		ReflectionTestUtils.setField(handler, "successRedirect", "/auth/login-complete");
		User user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123").build();
		when(kakaoUserService.findOrCreate(kakaoUser)).thenReturn(user);
		when(refreshTokenService.issue(user)).thenReturn("refresh-value");
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication());

		assertEquals("/auth/login-complete", response.getRedirectedUrl());
		assertTrue(response.getHeader("Set-Cookie").contains("REFRESH_TOKEN=refresh-value"));
		assertTrue(response.getHeader("Set-Cookie").contains("HttpOnly"));
		assertFalse(response.getHeader("Set-Cookie").contains("NR_ACCESS"));
	}

	@Test
	void postProcessingErrorRedirectsToFrontendWithCode() throws Exception {
		when(kakaoUserService.findOrCreate(kakaoUser))
			.thenThrow(new AuthException(AuthErrorCode.KAKAO_USER_ID_MISSING));
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication());

		assertEquals(FAILURE_URL + "?error=AUTH_KAKAO_USER502", response.getRedirectedUrl());
		assertNull(response.getHeader("Set-Cookie"));
		assertEquals("", response.getContentAsString());
	}

	@Test
	void oauthFailureRedirectsToFrontendWithCode() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		new KakaoLoginFailureHandler(redirector).onAuthenticationFailure(new MockHttpServletRequest(), response,
			new BadCredentialsException("denied"));

		assertEquals(FAILURE_URL + "?error=AUTH_KAKAO_LOGIN401", response.getRedirectedUrl());
	}

	private UsernamePasswordAuthenticationToken authentication() {
		return new UsernamePasswordAuthenticationToken(kakaoUser, null, List.of());
	}
}
