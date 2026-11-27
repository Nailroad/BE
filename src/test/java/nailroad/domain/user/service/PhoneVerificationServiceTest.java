package nailroad.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nailroad.domain.user.code.PhoneErrorCode;
import nailroad.domain.user.entity.PhoneSendLog;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.exception.PhoneException;
import nailroad.domain.user.repository.PhoneSendLogRepository;
import nailroad.domain.user.repository.PhoneVerificationRepository;
import nailroad.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PhoneVerificationServiceTest {

	private final PhoneVerificationRepository repository = mock(PhoneVerificationRepository.class);
	private final PhoneSendLogRepository sendLogRepository = mock(PhoneSendLogRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PhoneCodeSender sender = mock(PhoneCodeSender.class);
	private final PhoneVerificationService service =
		new PhoneVerificationService(repository, sendLogRepository, userRepository, sender);

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(service, "secret", "test-secret-32-bytes-minimum-length");
		ReflectionTestUtils.setField(service, "dailyLimitPerPhone", 5);
		ReflectionTestUtils.setField(service, "dailyLimitPerUser", 10);
		User user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123").build();
		ReflectionTestUtils.setField(user, "id", 42L);
		when(userRepository.findById(42L)).thenReturn(Optional.of(user));
		when(repository.findByUserIdForUpdate(42L)).thenReturn(Optional.empty());
	}

	@Test
	void sendsCodeAndRecordsLogUnderLimit() {
		when(sendLogRepository.countByPhoneAndSentAtAfter(eq("01012345678"), any())).thenReturn(4L);
		when(sendLogRepository.countByUserIdAndSentAtAfter(eq(42L), any())).thenReturn(9L);

		service.send(42L, "010-1234-5678");

		verify(sendLogRepository).save(any(PhoneSendLog.class));
		verify(sender).send(eq("01012345678"), anyString());
	}

	@Test
	void blocksWhenPhoneReachedDailyLimit() {
		when(sendLogRepository.countByPhoneAndSentAtAfter(eq("01012345678"), any())).thenReturn(5L);

		PhoneException exception = assertThrows(PhoneException.class, () -> service.send(42L, "010-1234-5678"));

		assertEquals(PhoneErrorCode.DAILY_LIMIT_EXCEEDED, exception.getErrorCode());
		verify(sender, never()).send(anyString(), anyString());
	}

	@Test
	void blocksWhenUserReachedDailyLimit() {
		when(sendLogRepository.countByUserIdAndSentAtAfter(eq(42L), any())).thenReturn(10L);

		PhoneException exception = assertThrows(PhoneException.class, () -> service.send(42L, "010-1234-5678"));

		assertEquals(PhoneErrorCode.DAILY_LIMIT_EXCEEDED, exception.getErrorCode());
		verify(sender, never()).send(anyString(), anyString());
	}
}
