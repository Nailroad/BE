package nailroad.domain.notification.controller;

import nailroad.domain.notification.dto.request.PushSettingRequest;
import nailroad.domain.notification.dto.response.PushSettingResponse;
import nailroad.domain.notification.service.PushSettingService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.handler.GlobalExceptionHandler;
import nailroad.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

	private final PushSettingService service = mock(PushSettingService.class);
	private final NotificationController controller = new NotificationController(service);
	private final User user = mock(User.class);

	// 로그인한 사용자의 푸시 알림 수신 여부를 그대로 응답하는지 확인한다.
	@Test
	void returnsPushSettingOfAuthenticatedUser() {
		when(user.isPushEnabled()).thenReturn(true);

		ApiResponse<PushSettingResponse> response = controller.getPushSetting(user);

		assertEquals("COMMON200", response.getCode());
		assertTrue(response.getResult().pushEnabled());
	}

	// 인증된 사용자가 없으면 푸시 설정을 조회하지 않고 인증 오류를 반환하는지 확인한다.
	@Test
	void rejectsGetPushSettingWithoutAuthentication() {
		CustomException exception = assertThrows(CustomException.class, () -> controller.getPushSetting(null));

		assertEquals(GlobalErrorCode.UNAUTHORIZED, exception.getErrorCode());
	}

	// 로그인한 사용자 ID와 요청한 수신 여부로 푸시 설정 변경을 요청하는지 확인한다.
	@Test
	void changesPushSettingOfAuthenticatedUser() {
		when(user.getId()).thenReturn(7L);
		when(service.change(7L, true)).thenReturn(new PushSettingResponse(true));

		ApiResponse<PushSettingResponse> response = controller.changePushSetting(user, new PushSettingRequest(true));

		assertEquals("COMMON200", response.getCode());
		assertTrue(response.getResult().pushEnabled());
		verify(service).change(7L, true);
	}

	// 인증된 사용자가 없으면 푸시 설정을 바꾸지 않고 인증 오류를 반환하는지 확인한다.
	@Test
	void rejectsChangePushSettingWithoutAuthentication() {
		CustomException exception = assertThrows(CustomException.class,
			() -> controller.changePushSetting(null, new PushSettingRequest(true)));

		assertEquals(GlobalErrorCode.UNAUTHORIZED, exception.getErrorCode());
		verifyNoInteractions(service);
	}

	// GET /api/v1/notifications/push-setting 경로가 푸시 설정 조회에 연결되는지 확인한다.
	@Test
	void mapsGetPushSettingRoute() throws Exception {
		mockMvc().perform(get("/api/v1/notifications/push-setting"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	// PATCH /api/v1/notifications/push-setting 경로가 푸시 설정 변경에 연결되는지 확인한다.
	@Test
	void mapsPatchPushSettingRoute() throws Exception {
		mockMvc().perform(patch("/api/v1/notifications/push-setting")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"pushEnabled\": true}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	// 수신 여부 값이 없으면 푸시 설정을 바꾸지 않고 입력값 오류를 반환하는지 확인한다.
	@Test
	void rejectsChangeWithoutPushEnabled() throws Exception {
		mockMvc().perform(patch("/api/v1/notifications/push-setting")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION400"));

		verifyNoInteractions(service);
	}

	private MockMvc mockMvc() {
		return MockMvcBuilders.standaloneSetup(controller)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}
}
