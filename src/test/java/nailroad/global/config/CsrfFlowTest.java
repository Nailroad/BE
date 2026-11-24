package nailroad.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import nailroad.domain.auth.controller.AuthController;
import nailroad.domain.auth.dto.TokenResponse;
import nailroad.domain.auth.service.AuthService.ReissueResult;
import nailroad.domain.auth.service.AuthService;
import nailroad.global.security.util.AuthCookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/** Swagger/프론트가 실제로 쓰는 순서(GET /auth/csrf → POST /auth/reissue)가 CSRF를 통과하는지 확인한다. */
@SpringJUnitWebConfig(CsrfFlowTest.Config.class)
class CsrfFlowTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.addFilter(context.getBean("springSecurityFilterChain", Filter.class))
			.build();
	}

	@Test
	void csrfEndpointReturnsSameValueAsCookieAndReissueAcceptsIt() throws Exception {
		MvcResult csrf = mockMvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
		Cookie xsrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
		assertNotNull(xsrfCookie);
		String body = csrf.getResponse().getContentAsString();
		assertEquals(true, body.contains("\"result\":\"" + xsrfCookie.getValue() + "\""));

		mockMvc.perform(post("/api/v1/auth/reissue")
				.cookie(xsrfCookie, new Cookie(AuthCookieUtil.REFRESH_COOKIE, "refresh"))
				.header("X-XSRF-TOKEN", xsrfCookie.getValue()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.accessToken").value("access"));
	}

	@Test
	void reissueWithoutCsrfHeaderIsForbidden() throws Exception {
		Cookie xsrfCookie = mockMvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");

		mockMvc.perform(post("/api/v1/auth/reissue")
				.cookie(xsrfCookie, new Cookie(AuthCookieUtil.REFRESH_COOKIE, "refresh")))
			.andExpect(status().isForbidden());
	}

	@Configuration
	@EnableWebMvc
	@EnableWebSecurity
	static class Config {

		@Bean
		SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			http.csrf(SecurityConfig::configureCsrf)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
			return http.build();
		}

		@Bean
		AuthController authController() {
			AuthService authService = mock(AuthService.class);
			when(authService.reissue(any())).thenReturn(
				new ReissueResult(new TokenResponse("access", "Bearer", 1800, null), "next-refresh"));
			return new AuthController(authService, new AuthCookieUtil(false, "Lax"));
		}
	}
}
