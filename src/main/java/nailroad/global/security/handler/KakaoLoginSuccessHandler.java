package nailroad.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.auth.service.KakaoUserService;
import nailroad.domain.auth.service.RefreshTokenService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.security.util.AuthCookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final KakaoUserService kakaoUserService;
	private final RefreshTokenService refreshTokenService;
	private final LoginFailureRedirector failureRedirector;
	private final AuthCookieUtil authCookieUtil;

	@Value("${app.auth.success-redirect}")
	private String successRedirect;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		try {
			User user = kakaoUserService.findOrCreate((OAuth2User) authentication.getPrincipal());
			String refreshToken = refreshTokenService.issue(user);
			completeLogin(request, response, refreshToken);
		} catch (AuthException exception) {
			log.warn("카카오 로그인 후처리 거절: {}", exception.getErrorCode().getCode());
			failureRedirector.redirect(request, response, exception.getErrorCode());
		} catch (RuntimeException exception) {
			log.error("카카오 로그인 후처리 실패", exception);
			failureRedirector.redirect(request, response, GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void completeLogin(HttpServletRequest request, HttpServletResponse response, String refreshToken)
			throws IOException {
		invalidateSession(request);
		response.addHeader(HttpHeaders.SET_COOKIE, authCookieUtil.refreshToken(refreshToken).toString());
		response.sendRedirect(successRedirect);
	}

	private void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
