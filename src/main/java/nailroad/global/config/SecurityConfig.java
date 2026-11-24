package nailroad.global.config;

import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.service.JwtService;
import nailroad.domain.user.repository.UserRepository;
import nailroad.global.security.filter.JwtAuthFilter;
import nailroad.global.security.handler.CustomAccessDenied;
import nailroad.global.security.handler.CustomEntryPoint;
import nailroad.global.security.handler.KakaoLoginFailureHandler;
import nailroad.global.security.handler.KakaoLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * 브라우저가 자동으로 붙이는 인증 수단은 리프레시 토큰 쿠키(path=/api/v1/auth)뿐이다.
	 * Bearer 토큰 API는 공격자가 헤더를 만들 수 없어 CSRF 대상이 아니므로, 쿠키로 동작하는 엔드포인트만 보호한다.
	 */
	static final RequestMatcher CSRF_PROTECTED = new OrRequestMatcher(
		PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/reissue"),
		PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/logout"));

	private final KakaoLoginSuccessHandler kakaoLoginSuccessHandler;
	private final KakaoLoginFailureHandler kakaoLoginFailureHandler;
	private final CustomEntryPoint customEntryPoint;
	private final CustomAccessDenied customAccessDenied;
	private final JwtService jwtService;
	private final UserRepository userRepository;

	/**
	 * spa(): XSRF-TOKEN 쿠키(JS에서 읽기 가능)를 발급하고, X-XSRF-TOKEN 헤더로 그 쿠키 값을 그대로 받는다.
	 * Swagger UI(springdoc.swagger-ui.csrf)와 axios가 쿠키 값을 헤더에 자동으로 실어 보내는 방식과 맞는다.
	 */
	static void configureCsrf(CsrfConfigurer<HttpSecurity> csrf) {
		csrf.spa().requireCsrfProtectionMatcher(CSRF_PROTECTED);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CORS 허용 범위는 CorsConfig(WebMvcConfigurer) 한 곳에서 관리하고, Security는 그 설정을 그대로 쓴다.
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(authorize -> authorize
				// 가입 미완료(ROLE_ONBOARDING) 회원도 써야 하는 경로라 /api/v1/** 규칙보다 먼저 둔다.
				.requestMatchers("/api/v1/auth/me", "/api/v1/onboarding/**").authenticated()
				.requestMatchers("/api/v1/auth/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/designs/**").permitAll()
				.requestMatchers("/api/v1/**").hasRole("USER")
				.anyRequest().permitAll())
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(customEntryPoint)
				.accessDeniedHandler(customAccessDenied))
			.csrf(SecurityConfig::configureCsrf)
			.oauth2Login(oauth2 -> oauth2
				.redirectionEndpoint(redirection -> redirection.baseUri("/auth/kakao/callback"))
				.successHandler(kakaoLoginSuccessHandler)
				.failureHandler(kakaoLoginFailureHandler))
			// 빈으로 등록하지 않아야 서블릿 필터로 중복 등록되지 않고, @WebMvcTest 슬라이스에도 끌려가지 않는다.
			.addFilterBefore(new JwtAuthFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
