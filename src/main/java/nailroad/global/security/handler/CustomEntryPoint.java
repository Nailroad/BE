package nailroad.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import nailroad.domain.user.code.AuthErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomEntryPoint implements AuthenticationEntryPoint {

	private final SecurityResponseUtil responseUtil;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		Object authErrorCode = request.getAttribute("authErrorCode");
		responseUtil.write(response, authErrorCode instanceof AuthErrorCode code ? code : AuthErrorCode.UNAUTHORIZED);
	}
}
