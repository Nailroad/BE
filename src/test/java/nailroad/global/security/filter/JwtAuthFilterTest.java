package nailroad.global.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.service.JwtService;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {

	private final JwtService jwtService = new JwtService("0123456789abcdef0123456789abcdef");
	private final UserRepository userRepository = mock(UserRepository.class);
	private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, userRepository);

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authenticatesValidBearerToken() throws Exception {
		User user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123").build();
		when(userRepository.findById(42L)).thenReturn(Optional.of(user));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.issue(42L));
		filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
			assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
			assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ONBOARDING")));
		});
	}

	@Test
	void rejectsInvalidBearerToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid");
		filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
			assertNull(SecurityContextHolder.getContext().getAuthentication());
			assertEquals(AuthErrorCode.INVALID_ACCESS_TOKEN, request.getAttribute("authErrorCode"));
		});
	}
}
