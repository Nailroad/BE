package nailroad.domain.user.service;

import lombok.RequiredArgsConstructor;
import nailroad.domain.user.dto.KakaoDTO;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoUserService {

	private final UserRepository userRepository;

	@Transactional
	public User findOrCreate(OAuth2User kakaoUser) {
		KakaoDTO kakao = KakaoDTO.from(kakaoUser);
		return userRepository.findByAuthProviderAndProviderUserId(AuthProvider.KAKAO, kakao.providerUserId())
			.orElseGet(() -> userRepository.save(User.builder()
				.authProvider(AuthProvider.KAKAO)
				.providerUserId(kakao.providerUserId())
				.name(kakao.name())
				.email(kakao.email())
				.build()));
	}
}
