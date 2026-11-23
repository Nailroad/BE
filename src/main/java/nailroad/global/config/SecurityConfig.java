package nailroad.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nailroad.global.security.handler.KakaoLoginSuccessHandler;
import nailroad.global.security.filter.JwtAuthFilter;
import nailroad.global.security.handler.CustomAccessDenied;
import nailroad.global.security.handler.CustomEntryPoint;
import nailroad.global.security.handler.KakaoLoginFailureHandler;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * 브라우저가 자동으로 붙이는 인증 수단은 리프레시 토큰 쿠키(path=/auth)뿐이다.
	 * Bearer 토큰 API는 공격자가 헤더를 만들 수 없어 CSRF 대상이 아니므로, 쿠키로 동작하는 엔드포인트만 보호한다.
	 */
	static final RequestMatcher CSRF_PROTECTED = new OrRequestMatcher(
		PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/reissue"),
		PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/logout"));

	private final KakaoLoginSuccessHandler kakaoLoginSuccessHandler;
	private final JwtAuthFilter jwtAuthFilter;
	private final CustomEntryPoint customEntryPoint;
	private final CustomAccessDenied customAccessDenied;
	private final KakaoLoginFailureHandler kakaoLoginFailureHandler;

	@Value("${app.auth.frontend-origin}")
	private String frontendOrigin;

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
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/v1/auth/me", "/api/v1/auth/phone/**",
								"/api/v1/auth/onboarding").authenticated()
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
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		if (frontendOrigin != null && !frontendOrigin.isBlank()) {
			configuration.setAllowedOrigins(List.of(frontendOrigin));
			configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
			configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
			configuration.setAllowCredentials(true);
		}
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
		FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}
}
