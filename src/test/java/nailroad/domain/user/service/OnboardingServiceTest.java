package nailroad.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nailroad.domain.auth.dto.LoginResponse;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.terms.entity.Terms;
import nailroad.domain.terms.exception.TermsException;
import nailroad.domain.terms.repository.AccountTermsRepository;
import nailroad.domain.terms.repository.TermsRepository;
import nailroad.domain.user.dto.CompleteOnboardingRequest.TermAgreement;
import nailroad.domain.user.dto.CompleteOnboardingRequest;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.enums.AccountType;
import nailroad.domain.user.enums.AuthProvider;
import nailroad.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OnboardingServiceTest {

	private final TermsRepository termsRepository = mock(TermsRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PhoneVerificationService phoneVerificationService = mock(PhoneVerificationService.class);
	private final OnboardingService service = new OnboardingService(termsRepository,
		mock(AccountTermsRepository.class), userRepository, phoneVerificationService);

	private User user;

	@BeforeEach
	void setUp() {
		user = User.builder().authProvider(AuthProvider.KAKAO).providerUserId("123").build();
		ReflectionTestUtils.setField(user, "id", 42L);
		when(userRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(user));
		when(termsRepository.findAll()).thenReturn(List.of(term(1L, true), term(2L, false)));
		when(phoneVerificationService.consumeVerified(42L, "010-1234-5678")).thenReturn("01012345678");
	}

	@Test
	void savesAccountTypeAndPhoneTogetherOnCompletion() {
		LoginResponse response = service.complete(42L, request(AccountType.SHOP_OWNER, true));

		assertEquals(AccountType.SHOP_OWNER, response.accountType());
		assertTrue(response.onboardingComplete());
		assertEquals("01012345678", user.getPhone());
	}

	@Test
	void cannotCompleteTwice() {
		service.complete(42L, request(AccountType.CUSTOMER, true));

		assertThrows(AuthException.class, () -> service.complete(42L, request(AccountType.SHOP_OWNER, true)));
		assertEquals(AccountType.CUSTOMER, user.getAccountType());
	}

	@Test
	void requiredTermMustBeAgreed() {
		assertThrows(TermsException.class, () -> service.complete(42L, request(AccountType.CUSTOMER, false)));
		assertEquals(null, user.getAccountType());
	}

	private CompleteOnboardingRequest request(AccountType type, boolean agreeRequired) {
		return new CompleteOnboardingRequest(type, "010-1234-5678",
			List.of(new TermAgreement(1L, agreeRequired), new TermAgreement(2L, false)));
	}

	private Terms term(Long id, boolean required) {
		Terms term = Terms.builder().termsVersion("v1").content("내용").required(required).build();
		ReflectionTestUtils.setField(term, "id", id);
		return term;
	}
}
