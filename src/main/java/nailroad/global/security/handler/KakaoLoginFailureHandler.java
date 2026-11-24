package nailroad.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nailroad.domain.auth.code.AuthErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginFailureHandler implements AuthenticationFailureHandler {

	private final LoginFailureRedirector failureRedirector;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		log.warn("카카오 로그인 실패: {}", exception.getMessage());
		failureRedirector.redirect(request, response, AuthErrorCode.KAKAO_LOGIN_FAILED);
	}
}
