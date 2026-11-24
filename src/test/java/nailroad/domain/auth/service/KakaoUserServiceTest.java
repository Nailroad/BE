package nailroad.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AccountType;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class KakaoUserServiceTest {

	private final UserRepository repository = mock(UserRepository.class);
	private final KakaoUserService service = new KakaoUserService(repository);
	private final DefaultOAuth2User kakaoUser = new DefaultOAuth2User(List.of(), Map.of("id", 123L), "id");

	@Test
	void firstLoginCreatesAccountWithoutType() {
		when(repository.findByAuthProviderAndProviderUserId(AuthProvider.KAKAO, "123")).thenReturn(Optional.empty());
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User user = service.findOrCreate(kakaoUser);
		assertNull(user.getAccountType());
	}

	@Test
	void laterLoginKeepsExistingAccountType() {
		User existing = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123")
			.accountType(AccountType.CUSTOMER).build();
		when(repository.findByAuthProviderAndProviderUserId(AuthProvider.KAKAO, "123"))
			.thenReturn(Optional.of(existing));

		assertEquals(existing, service.findOrCreate(kakaoUser));
	}
}
