package nailroad.domain.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import nailroad.domain.user.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class KakaoDTOTest {

	@Test
	void mapsKakaoIdWithoutEmailConsent() {
		var oauth2User = new DefaultOAuth2User(List.of(), Map.of(
			"id", 12345,
			"kakao_account", Map.of("profile", Map.of("nickname", "서영"))), "id");
		KakaoDTO kakao = KakaoDTO.from(oauth2User);
		assertEquals("12345", kakao.providerUserId());
		assertEquals("서영", kakao.name());
		assertNull(kakao.email());
	}

	@Test
	void rejectsMissingKakaoId() {
		var oauth2User = new DefaultOAuth2User(List.of(), Map.of("other", "value"), "other");
		assertThrows(AuthException.class, () -> KakaoDTO.from(oauth2User));
	}
}
