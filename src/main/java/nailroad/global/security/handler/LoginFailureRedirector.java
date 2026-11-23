package nailroad.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import nailroad.global.code.BaseErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인은 브라우저가 페이지째 이동하는 흐름이라 JSON 에러를 쓰면 사용자 화면에 그대로 노출된다.
 * 실패 시 프론트 주소로 돌려보내고, 원인은 {@code ?error=<에러 코드>}로만 전달한다.
 */
@Component
public class LoginFailureRedirector {

	private final String failureRedirect;

	public LoginFailureRedirector(@Value("${app.auth.failure-redirect}") String failureRedirect) {
		this.failureRedirect = failureRedirect;
	}

	public void redirect(HttpServletRequest request, HttpServletResponse response, BaseErrorCode errorCode)
			throws IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		String target = UriComponentsBuilder.fromUriString(failureRedirect)
			.queryParam("error", errorCode.getCode())
			.encode()
			.toUriString();
		response.sendRedirect(target);
	}
}
