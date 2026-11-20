package nailroad.domain.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
class ShopControllerTest {

	@Autowired
	private MockMvc mockMvc;

	// 인증 정보가 없는 등록 요청에 공통 인증 오류를 반환하는지 확인한다.
	@Test
	void returns401WithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/v1/shops").contentType(MediaType.APPLICATION_JSON).content("""
			{"shopName":"민서네일","shop_owner_phone":"010-0000-0000","shopPhone":"02-123-4567","shopAddress":"서울특별시 마포구 양화로 123"}
			"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	// 읽을 수 없는 JSON에 공통 요청 오류를 반환하는지 확인한다.
	@Test
	void returns400ForMalformedJson() throws Exception {
		mockMvc.perform(post("/api/v1/shops").contentType(MediaType.APPLICATION_JSON).content("{"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	// 필수 입력이 누락된 요청에 검증 오류를 반환하는지 확인한다.
	@Test
	void returnsValidationErrorForMissingFields() throws Exception {
		mockMvc.perform(post("/api/v1/shops").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION400"));
	}
}
