package nailroad.global.security.handler;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import nailroad.global.code.BaseErrorCode;
import nailroad.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityResponseUtil {

	private final ObjectMapper objectMapper;

	public void write(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ApiResponse.onFailure(errorCode, null));
	}
}
