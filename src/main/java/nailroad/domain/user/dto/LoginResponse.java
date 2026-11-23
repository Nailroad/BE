package nailroad.domain.user.dto;

import nailroad.domain.user.enums.AccountType;
import nailroad.domain.user.entity.User;

public record LoginResponse(Long userId, String name, String email, AccountType accountType,
		boolean onboardingComplete) {
	public static LoginResponse from(User user) {
		return new LoginResponse(user.getId(), user.getName(), user.getEmail(), user.getAccountType(),
			user.getOnboardingCompletedAt() != null);
	}
}
