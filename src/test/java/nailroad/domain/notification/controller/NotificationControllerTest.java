package nailroad.domain.notification.controller;

import java.util.List;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.notification.service.PushSettingService;
import nailroad.domain.user.entity.User;
import nailroad.global.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

	private final PushSettingService pushSettingService = mock(PushSettingService.class);
	private final MockMvc mockMvc = MockMvcBuilders
		.standaloneSetup(new NotificationController(pushSettingService))
		.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
		.setControllerAdvice(new GlobalExceptionHandler())
		.build();
	private final User user = mock(User.class);

	@BeforeEach
	void authenticate() {
		when(user.getId()).thenReturn(7L);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	// GET /api/v1/notifications/push-setting이 로그인한 사용자의 푸시 알림 수신 여부를 응답하는지 확인한다.
	@Test
	void getsPushSetting() throws Exception {
		when(user.isPushEnabled()).thenReturn(true);

		mockMvc.perform(get("/api/v1/notifications/push-setting"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.pushEnabled").value(true));
	}

	// PATCH /api/v1/notifications/push-setting이 로그인한 사용자 ID와 요청한 수신 여부로 변경을 요청하는지 확인한다.
	@Test
	void changesPushSetting() throws Exception {
		when(pushSettingService.change(7L, true)).thenReturn(new PushSettingResponse(true));

		mockMvc.perform(patch("/api/v1/notifications/push-setting")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"pushEnabled\": true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.pushEnabled").value(true));

		verify(pushSettingService).change(7L, true);
	}

	// 수신 여부 값이 없으면 푸시 설정을 바꾸지 않고 입력값 오류를 반환하는지 확인한다.
	@Test
	void rejectsChangeWithoutPushEnabled() throws Exception {
		mockMvc.perform(patch("/api/v1/notifications/push-setting")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION400"));

		verifyNoInteractions(pushSettingService);
	}
}
