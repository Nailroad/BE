package nailroad.domain.shop.controller;

import nailroad.domain.shop.service.ShopLikeService;
import nailroad.domain.user.entity.User;
import nailroad.global.code.GlobalErrorCode;
import nailroad.global.exception.CustomException;
import nailroad.global.handler.GlobalExceptionHandler;
import nailroad.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShopLikeControllerTest {

	private final ShopLikeService service = mock(ShopLikeService.class);
	private final ShopLikeController controller = new ShopLikeController(service);
	private final User user = mock(User.class);

	// 인증된 사용자 ID와 샵 ID로 찜을 요청하는지 확인한다.
	@Test
	void likesShopForAuthenticatedUser() {
		when(user.getId()).thenReturn(7L);

		ApiResponse<Void> response = controller.like(user, 17L);

		assertEquals("COMMON200", response.getCode());
		verify(service).like(7L, 17L);
	}

	// 인증된 사용자가 없으면 찜을 요청하지 않고 인증 오류를 반환하는지 확인한다.
	@Test
	void rejectsLikeWithoutAuthentication() {
		CustomException exception = assertThrows(CustomException.class, () -> controller.like(null, 17L));

		assertEquals(GlobalErrorCode.UNAUTHORIZED, exception.getErrorCode());
		verifyNoInteractions(service);
	}

	// PUT /api/v1/shops/{shopId}/like 경로가 찜 추가에 연결되는지 확인한다.
	@Test
	void mapsPutLikeRoute() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();

		mockMvc.perform(put("/api/v1/shops/17/like"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON401"));
	}
}
