package nailroad.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nailroad.domain.auth.entity.RefreshToken;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.auth.repository.RefreshTokenRepository;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class RefreshTokenServiceTest {

	private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
	private final RefreshTokenService service = new RefreshTokenService(repository);

	@Test
	void storesOnlyHashAndRotatesToken() {
		User user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123").build();
		String initial = service.issue(user);
		ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
		Mockito.verify(repository).save(captor.capture());
		RefreshToken stored = captor.getValue();
		String initialHash = stored.getTokenHash();
		assertNotEquals(initial, initialHash);
		when(repository.findByTokenHashForUpdate(initialHash)).thenReturn(Optional.of(stored));

		RefreshTokenService.RotatedToken rotated = service.rotate(initial);
		assertEquals(user, rotated.user());
		assertNotEquals(initial, rotated.value());
		assertNotEquals(initialHash, stored.getTokenHash());
	}

	@Test
	void rejectsUnknownToken() {
		when(repository.findByTokenHashForUpdate(any())).thenReturn(Optional.empty());
		assertThrows(AuthException.class, () -> service.rotate("unknown"));
	}
}
