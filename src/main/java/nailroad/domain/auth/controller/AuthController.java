package nailroad.domain.auth.controller;

import static nailroad.global.security.util.AuthCookieUtil.REFRESH_COOKIE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.dto.LoginResponse;
import nailroad.domain.auth.dto.TokenResponse;
import nailroad.domain.auth.service.AuthService.ReissueResult;
import nailroad.domain.auth.service.AuthService;
import nailroad.domain.user.entity.User;
import nailroad.global.response.ApiResponse;
import nailroad.global.security.util.AuthCookieUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final AuthCookieUtil authCookieUtil;

	@GetMapping("/login-complete")
	@Operation(summary = "카카오 로그인 완료 확인", description = "로컬 개발용 기본 리다이렉트 대상입니다. 프론트는 /api/v1/auth/reissue로 액세스 토큰을 받습니다.")
	public ApiResponse<LoginResponse> loginComplete(
			@Parameter(hidden = true) @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		return ApiResponse.onSuccess("카카오 로그인이 완료되었습니다.", authService.loginComplete(refreshToken));
	}

	@GetMapping("/login-failed")
	@Operation(summary = "카카오 로그인 실패 확인", description = "로컬 개발용 기본 실패 리다이렉트 대상입니다. 운영에서는 AUTH_FAILURE_REDIRECT로 프론트 주소를 지정합니다.")
	public ResponseEntity<ApiResponse<Void>> loginFailed(@RequestParam(required = false) String error) {
		AuthErrorCode failure = AuthErrorCode.KAKAO_LOGIN_FAILED;
		String code = error != null && error.matches("[A-Z0-9_]{1,64}") ? error : failure.getCode();
		return ResponseEntity.status(failure.getStatus())
			.body(ApiResponse.onFailure(code, failure.getMessage(), null));
	}

	@PostMapping("/reissue")
	@Operation(summary = "액세스·리프레시 토큰 재발급", description = "리프레시 토큰을 회전시키고 새 액세스 토큰을 발급합니다.")
	public ApiResponse<TokenResponse> reissue(
			@Parameter(hidden = true) @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletResponse response) {
		ReissueResult result = authService.reissue(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, authCookieUtil.refreshToken(result.refreshToken()).toString());
		return ApiResponse.onSuccess("토큰이 재발급되었습니다.", result.response());
	}

	@GetMapping("/me")
	@Operation(summary = "현재 로그인한 사용자 조회")
	public ApiResponse<LoginResponse> me(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		return ApiResponse.onSuccess("로그인한 사용자 정보를 조회했습니다.", LoginResponse.from(user));
	}

	@GetMapping("/csrf")
	@Operation(summary = "CSRF 토큰 조회",
		description = "XSRF-TOKEN 쿠키를 발급하고 같은 값을 돌려줍니다. /api/v1/auth/reissue, /api/v1/auth/logout 호출 시 X-XSRF-TOKEN 헤더에 넣습니다.")
	public ApiResponse<String> csrf(HttpServletRequest request) {
		// 마스킹된 요청 속성 값이 아니라 쿠키에 저장되는 원본 값을 준다. spa() 설정은 헤더에서 원본 값을 기대한다.
		DeferredCsrfToken csrfToken = (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
		return ApiResponse.onSuccess("CSRF 토큰을 조회했습니다.", csrfToken.get().getToken());
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하고 쿠키를 만료시킵니다. 토큰이 없어도 성공합니다.")
	public ApiResponse<Void> logout(@Parameter(hidden = true) @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletResponse response) {
		authService.logout(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, authCookieUtil.clearRefreshToken().toString());
		return ApiResponse.onSuccess("로그아웃되었습니다.");
	}
}
