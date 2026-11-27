package nailroad.domain.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nailroad.domain.auth.code.AuthErrorCode;
import nailroad.domain.auth.dto.LoginResponse;
import nailroad.domain.auth.exception.AuthException;
import nailroad.domain.terms.code.TermsErrorCode;
import nailroad.domain.terms.dto.TermsResponse;
import nailroad.domain.terms.entity.AccountTerms;
import nailroad.domain.terms.entity.Terms;
import nailroad.domain.terms.exception.TermsException;
import nailroad.domain.terms.repository.AccountTermsRepository;
import nailroad.domain.terms.repository.TermsRepository;
import nailroad.domain.user.dto.CompleteOnboardingRequest;
import nailroad.domain.user.entity.User;
import nailroad.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

	private final TermsRepository termsRepository;
	private final AccountTermsRepository accountTermsRepository;
	private final UserRepository userRepository;
	private final PhoneVerificationService phoneVerificationService;

	@Transactional(readOnly = true)
	public List<TermsResponse> terms() {
		return termsRepository.findAll().stream().map(TermsResponse::from).toList();
	}

	@Transactional
	public LoginResponse complete(Long userId, CompleteOnboardingRequest request) {
		User user = userRepository.findByIdForUpdate(userId)
			.orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
		if (user.getOnboardingCompletedAt() != null) {
			throw new AuthException(AuthErrorCode.ONBOARDING_ALREADY_COMPLETE);
		}
		List<Terms> terms = termsRepository.findAll();
		Map<Long, Boolean> submitted = validateAgreements(terms, request.agreements());
		String phone = phoneVerificationService.consumeVerified(userId, request.phone());
		LocalDateTime now = LocalDateTime.now();
		List<AccountTerms> records = new ArrayList<>();
		for (Terms term : terms) {
			Boolean agreed = submitted.get(term.getId());
			if (agreed != null) {
				records.add(AccountTerms.builder()
					.terms(term).user(user).agreed(agreed).recordedAt(now).build());
			}
		}
		accountTermsRepository.saveAll(records);
		user.completeOnboarding(request.accountType(), phone, now);
		return LoginResponse.from(user);
	}

	private Map<Long, Boolean> validateAgreements(List<Terms> terms,
			List<CompleteOnboardingRequest.TermAgreement> agreements) {
		if (terms.isEmpty() || terms.stream().noneMatch(term -> Boolean.TRUE.equals(term.getRequired()))) {
			throw new TermsException(TermsErrorCode.NOT_CONFIGURED);
		}
		Map<Long, Terms> known = new HashMap<>();
		for (Terms term : terms) {
			known.put(term.getId(), term);
		}
		Map<Long, Boolean> submitted = new HashMap<>();
		for (var agreement : agreements) {
			if (!known.containsKey(agreement.termId())) {
				throw new TermsException(TermsErrorCode.UNKNOWN_TERM);
			}
			if (submitted.putIfAbsent(agreement.termId(), agreement.agreed()) != null) {
				throw new TermsException(TermsErrorCode.DUPLICATE_TERM);
			}
		}
		for (Terms term : terms) {
			if (Boolean.TRUE.equals(term.getRequired()) && !Boolean.TRUE.equals(submitted.get(term.getId()))) {
				throw new TermsException(TermsErrorCode.REQUIRED_NOT_AGREED);
			}
		}
		return submitted;
	}
}
