package nailroad.domain.user.dto;

import java.util.Map;
import nailroad.domain.user.code.AuthErrorCode;
import nailroad.domain.user.exception.AuthException;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record KakaoDTO(String providerUserId, String name, String email) {

	public static KakaoDTO from(OAuth2User oauth2User) {
		Object id = oauth2User.getAttribute("id");
		if (id == null) {
			throw new AuthException(AuthErrorCode.KAKAO_USER_ID_MISSING);
		}
		Map<?, ?> account = oauth2User.getAttribute("kakao_account");
		String email = account != null && account.get("email") instanceof String value ? value : null;
		String name = null;
		if (account != null && account.get("profile") instanceof Map<?, ?> profile
				&& profile.get("nickname") instanceof String nickname) {
			name = nickname;
		}
		return new KakaoDTO(id.toString(), name, email);
	}
}
